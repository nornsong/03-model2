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
    commandMap.put("boardUpdate", new BoardUpdateCommand());
    commandMap.put("boardDelete", new BoardDeleteCommand());
    commandMap.put("signup", new SignupCommand());
    commandMap.put("login", new LoginCommand());
    commandMap.put("logout", new LogoutCommand());
  }

  public Command getCommand(String commandName) {
    return commandMap.get(commandName);
  }
}
