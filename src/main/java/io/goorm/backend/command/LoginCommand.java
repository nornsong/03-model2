package io.goorm.backend.command;

import io.goorm.backend.User;
import io.goorm.backend.UserDAO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.security.MessageDigest;

/**
 * 로그인 처리 Command
 */
public class LoginCommand implements Command {

  @Override
  public String execute(HttpServletRequest request, HttpServletResponse response) {
    try {
      if (request.getMethod().equals("GET")) {
        // 로그인 폼 표시
        return "/user/login.jsp";
      } else {
        // POST 요청 - 로그인 처리
        request.setCharacterEncoding("UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // 유효성 검사
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
          request.setAttribute("error", "아이디와 비밀번호를 모두 입력해주세요.");
          return "/user/login.jsp";
        }

        // 사용자 인증
        UserDAO userDAO = new UserDAO();
        User user = userDAO.getUserByUsername(username.trim());

        if (user == null) {
          request.setAttribute("error", "존재하지 않는 아이디입니다.");
          return "/user/login.jsp";
        }

        // 비밀번호 확인
        String hashedPassword = hashPassword(password);
        if (!user.getPassword().equals(hashedPassword)) {
          request.setAttribute("error", "비밀번호가 일치하지 않습니다.");
          return "/user/login.jsp";
        }

        // 로그인 성공 - 세션에 사용자 정보 저장
        HttpSession session = request.getSession();
        session.setAttribute("user", user);

        // 메인 페이지로 리다이렉트
        response.sendRedirect(request.getContextPath() + "/");
        return null;

      }

    } catch (Exception e) {
      request.setAttribute("error", "로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
      return "/user/login.jsp";
    }
  }

  /**
   * 간단한 비밀번호 해시 함수 (MD5)
   */
  private String hashPassword(String password) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] hash = md.digest(password.getBytes());
      StringBuilder sb = new StringBuilder();
      for (byte b : hash) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (Exception e) {
      e.printStackTrace();
      return password; // 해시 실패 시 원본 반환
    }
  }
}
