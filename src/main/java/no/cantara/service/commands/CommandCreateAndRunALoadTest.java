package no.cantara.service.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kevinsawicki.http.HttpRequest;
import no.cantara.service.commands.util.basecommands.BasePostCommand;
import no.cantara.service.model.TestSpecification;

public class CommandCreateAndRunALoadTest extends BasePostCommand<String> {
    private static final ObjectMapper mapper = new ObjectMapper();

	private String json;

    public CommandCreateAndRunALoadTest(String json) {
		this.json = json;
	}

    public CommandCreateAndRunALoadTest(TestSpecification testSpecification) {
        try {
            String loadTestJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(testSpecification);

            this.json = loadTestJson;
        } catch (Exception e) {
            log.error("Unable to serialice json for test specification", e);
        }
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
        return "loadTest/";
	}
}
