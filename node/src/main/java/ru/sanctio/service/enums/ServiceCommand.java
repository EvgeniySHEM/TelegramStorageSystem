package ru.sanctio.service.enums;

public enum ServiceCommand {
    HELP("/help"),
    REGISTRATION("/registration"),
    CANCEL("/cancel"),
    START("start");

    private final String value;

    ServiceCommand(String value) {
        this.value = value;
    }

    public static ServiceCommand fromValue(String text) {
        for (ServiceCommand command: ServiceCommand.values()) {
            if(command.value.equals(text)) {
                return command;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return value;
    }
    
}
