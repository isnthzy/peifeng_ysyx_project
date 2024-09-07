#include "../include/npc_common.h"
#include "../include/npc_debug.h"
#include "../include/npc_reg.h"
#include "../include/npc_memory.h"
#include <regex.h>
#include <assert.h>
#include <stdlib.h>

#define UNUSED(x) (void)(x)

int tokens_num=0; //放一个全局变量记录tokens的个数
enum {
  TK_NOTYPE = 256, TK_EQ,
  TK_NUM=1,TK_DEF=2,
  TK_NEG=3,TK_AND=4,
  TK_NEQ=5,TK_OR=6,
  TK_REG=7,TK_HEX=8,
  /* TODO: Add more token types */

};

static struct rule {
  const char *regex;
  int token_type;
} rules[] = {

  /* TODO: Add more rules.
   * Pay attention to the precedence level of different rules.
   */
  {"\\)", ')'},         // right_bracket
  {"\\(", '('},         // left_bracket
  {"\\/", '/'},         // divide
  {"\\*", '*'},         // multiply
  {"\\-", '-'},         // minus
  {"\\+", '+'},         // plus
  {" +", TK_NOTYPE},    // spaces
  {"\\=\\=", TK_EQ},        // equal
  {"\\&\\&", TK_AND},       // && 和
  {"\\|\\|", TK_OR},        // || 与 
  {"\\!\\=", TK_NEQ},       // not equal 不等于
  {"\\$[0-9a-z]*[0-9]*", TK_REG}, //识别寄存器类型,特殊0号寄存器是$0永远是0不用处理
  {"0[xX][0-9a-fA-F]+",TK_HEX}, //识别32位类型
  {"[0-9]*",TK_NUM},        // num  //"\\d+"不管用
};

#define NR_REGEX ARRLEN(rules)

static regex_t re[NR_REGEX] = {};

/* Rules are used for many times.
 * Therefore we compile them only once before any usage.
 */
void init_regex() {
  int i;
  char error_msg[128];
  int ret;

  for (i = 0; i < NR_REGEX; i ++) {
    ret = regcomp(&re[i], rules[i].regex, REG_EXTENDED);
    if (ret != 0) {
      regerror(ret, &re[i], error_msg, 128);
      panic("regex compilation failed: %s\n%s", error_msg, rules[i].regex);
    }
  }
}



typedef struct token {
  int type;
  char str[32];
} Token;

static Token tokens[10000] __attribute__((used)) = {};
static int nr_token __attribute__((used))  = 0;

bool check_parentheses(int p,int q){
  if(tokens[p].type!='('||tokens[q].type!=')') return false;
  int i=0;
  int left_c=0;
  int flag=0;
  for(i=p;i<=q;i++){
    if(tokens[i].type=='('){
      left_c++;
    }else if(tokens[i].type==')'){
      left_c--;
    }
    if(left_c==0&&i!=q) flag=1;
  }
  if(flag==1) return false;
  else return true;
} //括号匹配算法,注意坑((1+1)*2)与(1+1)*(1+1)后者不能直接去掉两边括号

int prio(int t){ //优先级排序,很重要!!!
  switch (t) {
      case TK_OR:
          return 7;
      case TK_AND:
          return 6;
      case TK_EQ:
      case TK_NEQ:
          return 5;
      case '+':
      case '-':
          return 4;
      case '*':
      case '/':
          return 3;
      case TK_DEF:
      case TK_NEG:
          return 2;
      default:
          return 0;
  }
}

word_t eval(int p,int q) {
  if (p > q) {
    assert(0);
    return -1;
    /* Bad expression */
  }
  else if (p == q) {
    /* Single token.
     * For now this token should be a number.
     * Return the value of the number.
     */
    return strtoul(tokens[p].str, NULL, 0); //strtoul将字符串转换为无符号长整型数
  }
  else if (check_parentheses(p, q) == true) {
    /* The expression is surrounded by a matched pair of parentheses.
     * If that is the case, just throw away the parentheses.
     */
    return eval(p + 1, q - 1);
  }
  else {
//    op = the position of 主运算符 in the token expression;
    int op=0;
    int pr=-1;
    int i;
    int val1=1;
    int bracketCount=0;
    for(i=p;i<=q;i++){
      if (tokens[i].type=='(') {
          bracketCount++;
          continue;
      } else if (tokens[i].type==')') {
          bracketCount--;
          if (bracketCount>0) {
              continue; // 如果还有未匹配的括号，则继续跳过
          }
      }
      if (bracketCount>0) {
          continue; // 在括号内部，跳过处理
      }
      if(tokens[i].type==TK_NUM||tokens[i].type==TK_NOTYPE||tokens[i].type==TK_HEX||tokens[i].type==TK_REG){
        continue;
      }else if(prio(tokens[i].type)>=pr){ //pr是当前最高优先级
        pr=prio(tokens[i].type);
        op=i;
      }
    }
    
    
    if(tokens[op].type!=TK_NEG&&tokens[op].type!=TK_DEF){
      val1 = eval(p, op - 1);
    }
    word_t val2 = eval(op + 1, q);

    switch (tokens[op].type) {
      case '+': return val1 + val2;
      case '-': return val1 - val2;
      case '*': return val1 * val2;
      case '/':
        if(val2==0){
          Log("Val2 is invalid(val=0)");
          assert(0);
        }
        else return val1 / val2;
      case TK_NEG: return val2=-val2;
      case TK_EQ:
        if(val1==val2) return 1;
        else return 0;
      case TK_NEQ:
        if(val1!=val2) return 1;
        else return 0;
      case TK_AND: return val1&&val2;
      case TK_OR : return val1||val2;
      case TK_DEF: return pmem_read(val2,4);
      default: assert(0);
    }
  }
}

static bool make_token(char *e) {
  int position = 0;
  int i;
  regmatch_t pmatch;
  memset(tokens, 0, sizeof(tokens)); //tokens清零,防止后续出现计算错误
  nr_token = 0;

  while (e[position] != '\0') {
    /* Try all rules one by one. */
    for (i = 0; i < NR_REGEX; i ++) {
      if (regexec(&re[i], e + position, 1, &pmatch, 0) == 0 && pmatch.rm_so == 0) {
        // char *substr_start = e + position;
        int substr_len = pmatch.rm_eo;

        // Log("match rules[%d] = \"%s\" at position %d with len %d: %.*s",
        //     i, rules[i].regex, position, substr_len, substr_len, substr_start);

        position += substr_len;

        /* TODO: Now a new token is recognized with rules[i]. Add codes
         * to record the token in the array `tokens'. For certain types
         * of tokens, some extra actions should be performed.
         */
        switch (rules[i].token_type) {
          case '(':
            tokens[nr_token++].type='(';
            break;
          case ')':
            tokens[nr_token++].type=')';
            break;
          case '/':
            tokens[nr_token++].type='/';
            break;
          case '*':
            tokens[nr_token++].type='*';
            break;
          case '-':
            tokens[nr_token++].type='-';
            break;
          case '+':
            tokens[nr_token++].type='+';
            break;
          case TK_NOTYPE:
            break;
          case TK_AND:
            tokens[nr_token++].type=TK_AND;
            break;
          case TK_OR:
            tokens[nr_token++].type=TK_OR;
            break;
          case TK_NEQ:
            tokens[nr_token++].type=TK_NEQ;
            break;
          case TK_EQ:
            tokens[nr_token++].type=TK_EQ;
            break;
          case TK_NUM:
            tokens[nr_token].type=TK_NUM;
            strncpy(tokens[nr_token++].str,&e[position-substr_len],substr_len);
            break;
          case TK_HEX:
            tokens[nr_token].type=TK_HEX;
            strncpy(tokens[nr_token++].str,&e[position-substr_len],substr_len);
            break;
          case TK_REG:
            tokens[nr_token].type=TK_REG;
            strncpy(tokens[nr_token++].str,&e[position-substr_len],substr_len);
            break;
            // tokens[nr_token++].str='1';
          default: TODO();
        }
        
        break;
      }
      
    }
    
    if (i == NR_REGEX) {
      printf("no match at position %d\n%s\n%*.s^\n", position, e, position, "");
      return false;
    }
  }
  tokens_num=nr_token;
  return true;
}


word_t expr(char *e, bool *success) {
  if (!make_token(e)) {
    *success = false;
    return 0;
  }
  int i;
  for (i=0;i<tokens_num;i++) {
    if (tokens[i].type == '-' && ( i == 0 || (tokens[i - 1].type!=')'&&tokens[i - 1].type != TK_NUM&&tokens[i-1].type!=TK_HEX&&tokens[i-1].type!=TK_REG)) ) {
      tokens[i].type = TK_NEG;
    }
    if (tokens[i].type == '*' && ( i == 0 || (tokens[i - 1].type!=')'&&tokens[i - 1].type != TK_NUM&&tokens[i-1].type!=TK_HEX&&tokens[i-1].type!=TK_REG)) ) {
      tokens[i].type = TK_DEF;
    }
    if (tokens[i].type == TK_REG){
        bool flag=true; 
        word_t reg_v=isa_reg_str2val(tokens[i].str,&flag);
        if(flag==false) assert(0);
        sprintf(tokens[i].str,"%u",reg_v);//用于将格式化的数据写入字符串中(str类型转化为u类型)
    }
  }
  /* TODO: Insert codes to evaluate the expression. */
  // printf("nr_token= %d\n",tokens_num);
  return eval(0,tokens_num-1);
  // printf("%d\n",tokens);
  // TODO();

  // return 0;
}
