package com.klasavchik.modelHorseProject.exception;

public class JudgingPhaseException extends RuntimeException {
    public static final String CODE = "NOT_IN_JUDGING_PHASE";

    public JudgingPhaseException() {
        super("Шоу не находится в фазе судейства");
    }

    public JudgingPhaseException(String message) {
        super(message);
    }
}
