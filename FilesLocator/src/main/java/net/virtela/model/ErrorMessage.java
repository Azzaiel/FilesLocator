package net.virtela.model;

import java.util.Objects;

public class ErrorMessage {
	private int code;
	private String message;

	public ErrorMessage() {
		super();
	}

	public ErrorMessage(int code, String message) {
		super();
		this.code = code;
		this.message = message;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		final StringBuffer content = new StringBuffer();
		content.append("Code :");
		content.append(this.code);
		content.append(", Message: ");
		content.append(this.message);
		return content.toString();
	}

	@Override
	public boolean equals(Object obj) {
        if (!(obj instanceof ErrorMessage)) {
            return false;
        }
        final ErrorMessage cmpObj = (ErrorMessage) obj;
		return Objects.equals(this.message, cmpObj.message)  && Objects.equals(this.code, cmpObj.code);
	}

	@Override
	public int hashCode() {
		return Objects.hash(message, code);
	}
}
