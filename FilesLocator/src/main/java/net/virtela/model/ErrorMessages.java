package net.virtela.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import net.virtela.constants.Constant;

public class ErrorMessages implements Serializable {

	private static final long serialVersionUID = 4801642684397151922L;

	private int code;
	private String referenceId;
	private List<ErrorMessage> errors = new ArrayList<>();

	public ErrorMessages() {
		super();
	}

	public ErrorMessages(int code) {
		super();
		this.code = code;
	}

	public ErrorMessages(int code, String referenceId, List<ErrorMessage> errors) {
		super();
		this.code = code;
		this.referenceId = referenceId;
		this.errors = errors;
	}
	
	public ErrorMessages(int code, String referenceId, ErrorMessage error) {
		this(code, referenceId,new ArrayList<>());
		this.errors.add(error);
	}
	
	public ErrorMessages(int code, String error) {
		this(code, null, new ArrayList<>());
		this.errors.add(new ErrorMessage(code, error));
	}
	
	public static ErrorMessages digest(int code,  ErrorMessage error) {
		final ErrorMessages errorMessages = new ErrorMessages(code);
		errorMessages.errors.add(error);
		return errorMessages;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public List<ErrorMessage> getErrors() {
		if (this.errors != null) {
			return this.errors;
		}
		return Collections.emptyList();
	}

	public String fetchErrorMessages() {
		final StringBuffer errorMessages = new StringBuffer();
		for (ErrorMessage error : this.errors) {
			errorMessages.append(error.getMessage()).append(Constant.DASH);
		}
		return errorMessages.toString();
	}

	public void setErrors(List<ErrorMessage> errors) {
		this.errors.clear();
		this.errors.addAll(errors);
	}

	public void addErrorMessage(ErrorMessage errorMessage) {
		if (errorMessage != null) {
			this.errors.add(errorMessage);
		}
	}
	
	public void addErrorMessage(ErrorMessage errorMessage, List<Long> siteIds) {
		if (errorMessage != null) {
			errorMessage.setMessage("Serial Numbers [" + StringUtils.join(siteIds.stream().sorted().collect(Collectors.toList()), Constant.COMMA + " ") + "]: " + errorMessage.getMessage());
			this.errors.add(errorMessage);
		}
	}
	
	public void addErrorMessage(ErrorMessage errorMessage, int position) {
		if (errorMessage != null) {
			this.errors.add(position, errorMessage);
		}
	}
	
	public void addAllErrorMessage(List<ErrorMessage> errMsgs) {
		if (errMsgs != null) {
			this.errors.addAll(errMsgs);
		}
	}

	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}

}
