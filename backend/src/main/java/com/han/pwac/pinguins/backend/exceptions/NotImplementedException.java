package com.han.pwac.pinguins.backend.exceptions;

/*
     Do not extend from GlobalException as this should just be displayed in the console
     which extending from GlobalException will prevent.
 */
public class NotImplementedException extends RuntimeException {
    public NotImplementedException() {
        super(createMessage());
    }

    private static String createMessage() {
        // gets the stack trace and walks over it to find the method where the exception occurred
        StackWalker.StackFrame stackFrame =  StackWalker.
                getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).
                walk(stream -> stream.skip(2).findFirst().get());

        return "The method at " + stackFrame.getDeclaringClass().getName() + "." + stackFrame.getMethodName() + "(" + stackFrame.getDeclaringClass().getSimpleName() + ".java:" + stackFrame.getLineNumber() + ")" + " is not implemented";
    }
}
