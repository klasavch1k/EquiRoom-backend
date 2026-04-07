package com.klasavchik.modelHorseProject.exception;

public class ShowReadOnlyException extends RuntimeException {
    public static final String CODE = "SHOW_READ_ONLY_PERIOD";

    public ShowReadOnlyException(String message) {
        super(message);
    }

    public ShowReadOnlyException() {
        super("Шоу в периоде подведения итогов или завершено: доступен только просмотр");
    }
}
