package no.cantara.service.commands;


import no.cantara.service.commands.util.basecommands.BaseGetCommand;

public class CommandGetAllLoadTestResults extends BaseGetCommand<String> {
	@Override
	protected String getTargetPath() {
		return "loadTest/status/";
	}
}
