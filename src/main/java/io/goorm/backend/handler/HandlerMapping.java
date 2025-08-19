package io.goorm.backend.handler;

import io.goorm.backend.command.*;

import java.util.HashMap;
import java.util.Map;

public class HandlerMapping {

  private Map<String, Command> commandMap;

  public HandlerMapping() {
    commandMap = new HashMap<>();
    commandMap.put("boardList", new BoardListCommand());
    commandMap.put("boardWrite", new BoardWriteCommand());
    commandMap.put("boardInsert", new BoardInsertCommand());
    commandMap.put("boardView", new BoardViewCommand());
  }

  public Command getCommand(String commandName) {
    return commandMap.get(commandName);
  }
}
