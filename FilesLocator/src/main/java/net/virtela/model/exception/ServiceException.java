package net.virtela.model.exception;


import net.virtela.constants.Constant;
import net.virtela.model.ErrorMessage;
import net.virtela.model.ErrorMessages;

public class ServiceException extends Exception {

	private static final long serialVersionUID = 7686982862582621828L;

	private ErrorMessages errorMessages;
	private String error;

	public ServiceException() {
		super();
		this.errorMessages = new ErrorMessages(406);
	}

	public ServiceException(String error) {
		super();
		this.error = error;
	}

	public ServiceException(ErrorMessages errorMessages) {
		super();
		this.errorMessages = errorMessages;
	}

	public void setErrorMessages(ErrorMessages errorMessages) {
		this.errorMessages = errorMessages;
	}

	public ErrorMessages getErrorMessages() {
		if (this.errorMessages != null) {
			return this.errorMessages;
		}
		return new ErrorMessages();
	}

	public int getHttpErrorCode() {
		return errorMessages.getCode();
	}

	@Override
	public String toString() {
		final StringBuffer errors = new StringBuffer();
		if (this.errorMessages != null) {
			errors.append("HTTP ERROR CODE: " + this.errorMessages.getCode());
			for (ErrorMessage errorMessage : this.errorMessages.getErrors()) {
				errors.append(errorMessage);
				errors.append(Constant.SEMI_COLON);
				errors.append(Constant.SPACE);
			}
		}
		return errors.toString();
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

}
