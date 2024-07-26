package jpabook.jpashop.bizException;

public class BizException extends RuntimeException{

    protected String code = "500";
    
    private static final long serialVersionUID = -817795158053038544L;
    private static final String EXCEPTION_CODE = "400";
    
    


    public BizException(String message) {
        super(message);
        this.code = "400";

    }


}
