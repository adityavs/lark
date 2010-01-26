package com.stuffwithstuff.lark;

import java.util.ArrayList;
import java.util.List;

public class SpecialForms {

    public static CallableExpr quote() {
        return new CallableExpr() {
                public Expr call(Interpreter interpreter, Environment environment, Expr argExpr) {
                    return argExpr;
                }
            };
    }

    public static CallableExpr doForm() {
        return new CallableExpr() {
                public Expr call(Interpreter interpreter, Environment environment, Expr argExpr) {
                    // if the arg isn't a list, just eval it normally
                    if (!(argExpr instanceof ListExpr)) return interpreter.eval(environment, argExpr);
                    
                    // evaluate each item in the arg list in order, returning the last one
                    ListExpr argList = (ListExpr)argExpr;
                    
                    Expr result = null;
                    for (Expr arg : argList.getList()) {
                        result = interpreter.eval(environment, arg);
                    }
                    
                    return result;
                }
            };
    }

    public static CallableExpr print() {
        return new CallableExpr() {
                public Expr call(Interpreter interpreter, Environment environment, Expr argExpr) {
                    Expr arg = interpreter.eval(environment, argExpr);
                    System.out.println(arg);

                    return Expr.unit();
                }
            };
    }

    public static CallableExpr createFunction() {
        return new CallableExpr() {
                public Expr call(Interpreter interpreter, Environment environment, Expr argExpr) {
                    return createFunction(false, argExpr);
                }
            };
    }
    
    public static CallableExpr createMacro() {
        return new CallableExpr() {
                public Expr call(Interpreter interpreter, Environment environment, Expr argExpr) {
                    return createFunction(true, argExpr);
                }
            };
    }
    
    public static CallableExpr defIs() {
        return new CallableExpr() {
                public Expr call(Interpreter interpreter, Environment environment, Expr argExpr) {
                    //### bob: need lots of error-checking all through here!
                    ListExpr args = (ListExpr)argExpr;
                    
                    String name = ((NameExpr)args.getList().get(0)).getName();
                    Expr body = args.getList().get(1);
                    
                    // define the name in the current scope
                    environment.put(name, interpreter.eval(environment, body));
                    
                    return Expr.unit();
                }
            };
    }
    
    public static CallableExpr ifThen() {
        return new CallableExpr() {
                public Expr call(Interpreter interpreter, Environment scope, Expr argExpr) {
                    if (!(argExpr instanceof ListExpr)) return interpreter.error("'if:then:' expects an argument list.");
                    
                    ListExpr argListExpr = (ListExpr)argExpr;
                    if (argListExpr.getList().size() != 2) return interpreter.error ("'if:then:' expects two arguments.");
                    
                    // evaluate the condition
                    Expr condition = interpreter.eval(scope, argListExpr.getList().get(0));
                    
                    if (!(condition instanceof BoolExpr)) return interpreter.error("'if:then:' condition must evaluate to true or false.");
                    
                    // evaluate the then branch 
                    if (((BoolExpr)condition).getValue()) {
                        return interpreter.eval(scope, argListExpr.getList().get(1));
                    } else {
                        // condition was false
                        return Expr.unit();
                    }
                }
            };
    }
    
    public static CallableExpr ifThenElse() {
        return new CallableExpr() {
                public Expr call(Interpreter interpreter, Environment scope, Expr argExpr) {
                    if (!(argExpr instanceof ListExpr)) return interpreter.error("'if:then:else:' expects an argument list.");
                    
                    ListExpr argListExpr = (ListExpr)argExpr;
                    if (argListExpr.getList().size() != 3) return interpreter.error ("'if:then:else:' expects three arguments.");
                    
                    // evaluate the condition
                    Expr condition = interpreter.eval(scope, argListExpr.getList().get(0));
                    
                    if (!(condition instanceof BoolExpr)) return interpreter.error("'if:then:else:' condition must evaluate to true or false.");
                    
                    // evaluate the then branch 
                    if (((BoolExpr)condition).getValue()) {
                        return interpreter.eval(scope, argListExpr.getList().get(1));
                    } else {
                        // condition was false
                        return interpreter.eval(scope, argListExpr.getList().get(2));
                    }
                }
            };
    }
    
    public static CallableExpr boolPredicate() {
        return new CallableExpr() {
                public Expr call(Interpreter interpreter, Environment scope, Expr argExpr) {
                    Expr arg = interpreter.eval(scope, argExpr);
                    
                    return new BoolExpr(arg instanceof BoolExpr);
                }
            };
    }
    
    public static CallableExpr intPredicate() {
        return new CallableExpr() {
                public Expr call(Interpreter interpreter, Environment scope, Expr argExpr) {
                    Expr arg = interpreter.eval(scope, argExpr);
                    
                    return new BoolExpr(arg instanceof IntExpr);
                }
            };
    }
    
    public static CallableExpr listPredicate() {
        return new CallableExpr() {
                public Expr call(Interpreter interpreter, Environment scope, Expr argExpr) {
                    Expr arg = interpreter.eval(scope, argExpr);
                    
                    return new BoolExpr(arg instanceof ListExpr);
                }
            };
    }
    
    public static CallableExpr namePredicate() {
        return new CallableExpr() {
                public Expr call(Interpreter interpreter, Environment scope, Expr argExpr) {
                    Expr arg = interpreter.eval(scope, argExpr);
                    
                    return new BoolExpr(arg instanceof NameExpr);
                }
            };
    }
    
    public static CallableExpr unitPredicate() {
        return new CallableExpr() {
                public Expr call(Interpreter interpreter, Environment scope, Expr argExpr) {
                    Expr arg = interpreter.eval(scope, argExpr);
                    
                    // unit is the empty list
                    return new BoolExpr((arg instanceof ListExpr) &&
                            (((ListExpr)arg).getList().size() == 0));
                }
            };
    }
    
    public static CallableExpr count() {
        return new CallableExpr() {
                public Expr call(Interpreter interpreter, Environment scope, Expr argExpr) {
                    Expr arg = interpreter.eval(scope, argExpr);
                    
                    if (!(arg instanceof ListExpr)) return interpreter.error("Argument to 'count' must be a list.");
                    
                    ListExpr list = (ListExpr)arg;
                    return new IntExpr(list.getList().size());
                }
            };
    }
    
    private static Expr createFunction(boolean isMacro, Expr arg) {
        //### bob: need lots of error-handling here
        ListExpr argList = (ListExpr)arg;
        
        // get the parameter name(s)
        List<String> paramNames = new ArrayList<String>();
        Expr parameters = argList.getList().get(0);
        if (parameters instanceof ListExpr) {
            ListExpr paramList = (ListExpr)parameters;
            
            for (Expr param : paramList.getList()) {
                paramNames.add(((NameExpr)param).getName());
            }
        } else {
            // not a list, so assume it's a single name
            paramNames.add(((NameExpr)parameters).getName());
        }
        
        // create the function
        return new FunctionExpr(isMacro, paramNames, argList.getList().get(1));
        //### bob: need to support closures at some point
    }
}