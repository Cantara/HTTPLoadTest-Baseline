package no.cantara.service.commands;

import com.github.kevinsawicki.http.HttpRequest;
import no.cantara.service.commands.util.basecommands.BasePostCommand;

public class CommandCreateApplication extends BasePostCommand<String> {
	
	private String json;
	
	public CommandCreateApplication(String json){
		this.json = json;
	}
	
	@Override
	protected HttpRequest dealWithRequestBeforeSend(HttpRequest request) {
		super.dealWithRequestBeforeSend(request);
		request.contentType("application/json").send(json);
		return request;
	}
	
	@Override
	protected String dealWithFailedResponse(String responseBody, int statusCode) {
		return statusCode + ":" + responseBody;
	}
	
	@Override
	protected String dealWithResponse(String response) {
		return "200" + ":" + super.dealWithResponse(response);
	}
	
	@Override
	protected String getTargetPath() {
		return "application/";
	}
}
