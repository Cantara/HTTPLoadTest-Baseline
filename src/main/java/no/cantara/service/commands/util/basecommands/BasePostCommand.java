package  no.cantara.service.commands.util.basecommands;

import com.github.kevinsawicki.http.HttpRequest;
import no.cantara.base.command.BaseHttpPostHystrixCommand;
import no.cantara.base.command.HttpSender;
import no.cantara.service.commands.config.ConfigValue;
import no.cantara.service.commands.config.ConstantValue;

import java.net.URI;
import java.util.Base64;

public abstract class BasePostCommand<T> extends BaseHttpPostHystrixCommand<T> {

	public BasePostCommand(String hystrixGroupKey) {
		super(URI.create(ConfigValue.CONFIGSERVICE_URL), hystrixGroupKey, ConstantValue.COMMAND_TIMEOUT);
	}
	
	public BasePostCommand() {
		super(URI.create(ConfigValue.CONFIGSERVICE_URL), "command_group", ConstantValue.COMMAND_TIMEOUT);
	}
	
	@Override
	protected HttpRequest dealWithRequestBeforeSend(HttpRequest request) {
		String usernameAndPassword = ConfigValue.CONFIGSERVICE_USERNAME + ":" + ConfigValue.CONFIGSERVICE_PASSWORD;
		String encoded = Base64.getEncoder().encodeToString(usernameAndPassword.getBytes());
		request.authorization("Basic " + encoded);
		request.contentType(HttpSender.APPLICATION_JSON);
		return super.dealWithRequestBeforeSend(request);
	}
	
	
}
