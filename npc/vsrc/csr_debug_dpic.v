
import "DPI-C" function void Csr_assert();
module csr_debug_dpic(
    input        clock,
    input        reset,
    input        csr_valid,
    input        assert_wen
);
 always @(posedge clock)begin
   if(~reset)begin
     if(csr_valid&&assert_wen)  Csr_assert();
   end
  end
endmodule
    
