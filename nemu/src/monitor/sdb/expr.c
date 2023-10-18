/***************************************************************************************
* Copyright (c) 2014-2022 Zihao Yu, Nanjing University
*
* NEMU is licensed under Mulan PSL v2.
* You can use this software according to the terms and conditions of the Mulan PSL v2.
* You may obtain a copy of Mulan PSL v2 at:
*          http://license.coscl.org.cn/MulanPSL2
*
* THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
* EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
* MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
*
* See the Mulan PSL v2 for more details.
***************************************************************************************/

#include <isa.h>

/* We use the POSIX regex functions to process regular expressions.
 * Type 'man regex' for more information about POSIX regex functions.
 */
#include <regex.h>
#define min(a, b) (((a) < (b)) ? (a) : (b))
int tokens_num; //放一个全局变量记录token的个数
enum {
  TK_NOTYPE = 256, TK_EQ,
  TK_NUM=1
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
  {"==", TK_EQ},        // equal
  {"[0-9]*",TK_NUM},    // num  //"\\d+"不管用
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

static Token tokens[32] __attribute__((used)) = {};
static int nr_token __attribute__((used))  = 0;

bool check_parentheses(int p,int q){
  if(tokens[p].type!='('||tokens[q].type!=')') return false;
  int l=p,r=q;
  while(l<r){
    if(tokens[p].type=='('){
      if(tokens[p].type==')'){
        l++,r--;
        continue;
      }else{
        r--;
      }
    }else if(tokens[p].type==')'){
      return false;
    }else l++;

  }
  return true;
}

int prio(int t){
  switch (t) {
      case '+':
      case '-':
          return 1;
      case '*':
      case '/':
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
    return atoi(tokens[p].str);
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
    int i,j;
    for(i=p;i<=q;i++){
      if(tokens[i].type=='('){
        for(j=i+1;j<=q;j++){
          if(tokens[j].type==')') i=j;
        }
      }else if(tokens[i].type==TK_NUM){
        continue;
      }else if(prio(tokens[i].type)>pr){ //pr是当前最高优先级
        pr=prio(tokens[i].type);
        op=i;
      }
    }
    
    
    word_t val1 = eval(p, op - 1);
    word_t val2 = eval(op + 1, q);

    switch (tokens[op].type) {
      case '+': return val1 + val2;
      case '-': return val1 - val2;
      case '*': return val1 * val2;
      case '/': return val1 / val2;
      default: assert(0);
    }
  }
}

static bool make_token(char *e) {
  int position = 0;
  int i;
  regmatch_t pmatch;

  nr_token = 0;

  while (e[position] != '\0') {
    /* Try all rules one by one. */
    for (i = 0; i < NR_REGEX; i ++) {
      if (regexec(&re[i], e + position, 1, &pmatch, 0) == 0 && pmatch.rm_so == 0) {
        char *substr_start = e + position;
        int substr_len = pmatch.rm_eo;

        Log("match rules[%d] = \"%s\" at position %d with len %d: %.*s",
            i, rules[i].regex, position, substr_len, substr_len, substr_start);

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
          case TK_NUM:
            tokens[nr_token].type=TK_NUM;
            strncpy(tokens[nr_token++].str,&e[position-substr_len],substr_len);
            break;
            // tokens[nr_token++].str='1';
          default: TODO();
        }
        tokens_num=nr_token;
        break;
      }
    }

    if (i == NR_REGEX) {
      printf("no match at position %d\n%s\n%*.s^\n", position, e, position, "");
      return false;
    }
  }

  return true;
}


word_t expr(char *e, bool *success) {
  if (!make_token(e)) {
    *success = false;
    return 0;
  }

  /* TODO: Insert codes to evaluate the expression. */
  word_t a=eval(0,tokens_num-1);
  printf("%u",a);
  // TODO();

  return 0;
}
