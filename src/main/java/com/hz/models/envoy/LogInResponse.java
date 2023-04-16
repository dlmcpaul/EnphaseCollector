package com.hz.models.envoy;

// {"message":"success",
// "session_id":"f9aebf5d9d4f11c6fce7f269075a2d6e",
// "manager_token":"eyJhbGciOiJIUzI1NiJ9.eyJkYXRhIjp7InNlc3Npb25faWQiOiJmOWFlYmY1ZDlkNGYxMWM2ZmNlN2YyNjkwNzVhMmQ2ZSIsImNvbXBhbnlfaWQiOm51bGwsImVtYWlsX2lkIjoiZGxtY3BhdWxAZ21haWwuY29tIiwidXNlcl9pZCI6OTM2NDY1LCJjbGllbnRfYXBwIjoiaXRrMyIsImZpcnN0X25hbWUiOiJEYXZpZCIsImxhc3RfbmFtZSI6Ik1jUGF1bCIsImxvZ2luX3VzZXIiOm51bGwsImlzX2Rpc3RyaWJ1dG9yIjpmYWxzZX0sImV4cCI6MTY4MjI1MDk5NSwic3ViIjoiZGxtY3BhdWxAZ21haWwuY29tIn0.TS5QXls4_XmHsbeHxkFAWcLfIrJ7bUjFytLdjEgZUtE",
// "is_consumer":true}

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LogInResponse {
	String message;
	@JsonProperty(value="session_id")
	String sessionId;
	@JsonProperty(value="manager_token")
	String managerToken;
	@JsonProperty(value="is_consumer")
	boolean isConsumer;

	public boolean isSuccess() {
		return message.equalsIgnoreCase("success");
	}
}
