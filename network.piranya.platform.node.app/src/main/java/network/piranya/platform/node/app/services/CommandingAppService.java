package network.piranya.platform.node.app.services;

import java.util.List;
import java.util.Map;

import network.piranya.platform.api.exceptions.FeatureImplementationNotFoundException;
import network.piranya.platform.api.exceptions.InvalidParameterException;
import network.piranya.platform.api.exceptions.UnexpectedException;
import network.piranya.platform.api.extension_models.ActionType;
import network.piranya.platform.api.extension_models.Parameters;
import network.piranya.platform.api.extension_models.app.AppService;
import network.piranya.platform.api.extension_models.app.AppServiceMetadata;
import network.piranya.platform.api.extension_models.app.Operation;
import network.piranya.platform.api.lang.None;
import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.api.lang.Result;
import network.piranya.platform.api.lang.ResultHandler;
import network.piranya.platform.api.models.bots.BotRef;
import network.piranya.platform.api.models.bots.BotSpec;
import network.piranya.platform.api.models.bots.BotView;
import network.piranya.platform.api.models.metadata.BotTypeInfo;
import network.piranya.platform.api.models.metadata.CommandInfo;
import network.piranya.platform.node.utilities.StringUtils;

@AppServiceMetadata(id = "CommandingAppService")
public class CommandingAppService extends AppService {
	
	@Operation("invoke_command")
	public void invokeCommand(Parameters params, ResultHandler<Object> resultHandler) {
		try {
			String hostRef = params.string("core:host_ref");
			String commandId = params.string("core:command_id");
			if (hostRef.contains(":")) {
				BotRef botRef = new BotRef(hostRef);
				context().bots().execution().invokeBot(botRef, commandId, params, result -> {
					if (result.isSuccessful()) resultHandler.accept(new Result<>(result.result().get())); else resultHandler.accept(new Result<>(result.error().get()));
				});
			} else {
				BotTypeInfo botTypeInfo = context().bots().botMetadata(hostRef);
				if (botTypeInfo.getType() == BotTypeInfo.Type.COMMAND) {
					context().bots().commands().invokeCommandBot(BotSpec.byType(botTypeInfo.getBotTypeId()), commandId, params, resultHandler);
				} else if (botTypeInfo.getType() == BotTypeInfo.Type.EXECUTION) {
					context().bots().execution().startBot(BotSpec.byType(botTypeInfo.getBotTypeId()), params, result -> {
						if (result.isSuccessful()) {
							resultHandler.accept(new Result<>(new BotRefObject(result.result().get().ref())));
						} else {
							resultHandler.accept(new Result<>(result.error().get()));
						}
					});
				} else {
					throw new UnexpectedException(String.format("Bot Type '%s' is not supported", botTypeInfo.getType()));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			resultHandler.accept(new Result<>(ex));
		}
	}
	
	@Operation("retrieve_command_details")
	public void retrieveCommandInfo(Parameters params, ResultHandler<CommandDetails> resultHandler) {
		String actionDescriptor = params.string("action_descriptor");
		try {
			String runPrefix = ActionType.RUN_BOT.prefix() + "!";
			if (actionDescriptor.startsWith(runPrefix)) {
				String action = actionDescriptor.substring(runPrefix.length());
				int paramsIndex = action.indexOf("|");
				if (paramsIndex >= 0) {
					action = action.substring(0, paramsIndex);
				}
				try {
					resultHandler.accept(new Result<>(getCommandInfoForAction(action)));
				} catch (FeatureImplementationNotFoundException ex) {
					resultHandler.accept(new Result<>(ex));
				}
			} else {
				resultHandler.accept(new Result<>(new UnexpectedException(String.format("Action descriptor '%s' is invalid", actionDescriptor))));
			}
		} catch (Throwable ex) {
			ex.printStackTrace();
			resultHandler.accept(new Result<>(new UnexpectedException(ex)));
		}
	}
	
	@Operation("validate_command_params")
	public void validateCommandParams(Parameters params, ResultHandler<None> resultHandler) {
		String actionDescriptor = params.string("action_descriptor");
		try {
			String runPrefix = ActionType.RUN_BOT.prefix() + "!";
			if (actionDescriptor.startsWith(runPrefix)) {
				String action = actionDescriptor.substring(runPrefix.length());
				try {
					CommandInfo commandInfo = getCommandInfoForAction(action).getCommandInfo();
					params.validate(commandInfo.getInputs());
					resultHandler.accept(new Result<>(None.VALUE));
				} catch (FeatureImplementationNotFoundException ex) {
					resultHandler.accept(new Result<>(ex));
				} catch (InvalidParameterException ex) {
					resultHandler.accept(new Result<>(ex));
				}
			} else {
				resultHandler.accept(new Result<>(new UnexpectedException(String.format("Action descriptor '%s' is invalid", actionDescriptor))));
			}
		} catch (Throwable ex) {
			ex.printStackTrace();
			resultHandler.accept(new Result<>(new UnexpectedException(ex)));
		}
	}
	
	@Operation("retrieve_running_bot_details")
	public void retrieveBotInputsAndParams(Parameters params, ResultHandler<RunningBotDetails> resultHandler) {
		try {
			String botId = params.string("bot_id");
			BotView botView = context().bots().execution().bot(new BotRef(botId));
			BotTypeInfo botTypeInfo = context().bots().botMetadata(botView.botTypeId());
			resultHandler.accept(new Result<>(new RunningBotDetails(botId, botTypeInfo, botView.params().dataMap())));
		} catch (Throwable ex) {
			resultHandler.accept(new Result<>(new UnexpectedException(ex)));
		}
	}
	
	
	protected CommandDetails getCommandInfoForAction(String action) throws FeatureImplementationNotFoundException {
		if (action.contains(".")) { /// is bot type
			if (action.contains("/")) { /// is command bot invocation
				String[] parts = StringUtils.splitString(action, '/');
				String botTypeId = parts[0];
				String commandId = parts[1];
				BotTypeInfo botTypeInfo = context().bots().botMetadata(botTypeId);
				return new CommandDetails(botTypeInfo.getBotTypeId(), utils.col.find(utils.col.toList(botTypeInfo.getCommands()), c -> c.getCommandId().equals(commandId)).get());
			} else { /// is start execution bot
				String botTypeId = action;
				BotTypeInfo botTypeInfo = context().bots().botMetadata(botTypeId);
				return new CommandDetails(botTypeInfo.getBotTypeId(),
						new CommandInfo("-start", botTypeInfo.getDisplayName(), botTypeInfo.getDescription(), botTypeInfo.getInputs(), new String[0], botTypeInfo.getSearchInfo(), 0));
			}
		} else if (action.contains(":")) { /// is execution bot invocation
			String[] parts = StringUtils.splitString(action, '/');
			String botId = parts[0];
			String botTypeId = context().bots().execution().bot(new BotRef(botId)).botTypeId();
			String commandId = parts[1];
			return new CommandDetails(botId, utils.col.find(utils.col.toList(context().bots().botMetadata(botTypeId).getCommands()), c -> c.getCommandId().equals(commandId)).get());
		} else {
			String featureId = action;
			List<BotTypeInfo> botTypes = context().bots().execution().botTypesByFeature(featureId);
			List<BotTypeInfo> suitableCommandBots = utils.col.filter(botTypes, bt -> bt.getType() == BotTypeInfo.Type.COMMAND && findCommandByFeature(bt, featureId).isPresent());
			if (!suitableCommandBots.isEmpty()) {
				return findCommandByFeature(suitableCommandBots.get(0), featureId).get();
			} else {
				List<BotTypeInfo> suitableExecutionBots = utils.col.filter(botTypes, bt -> bt.getType() == BotTypeInfo.Type.EXECUTION);
				if (!suitableExecutionBots.isEmpty()) {
					BotTypeInfo botTypeInfo = suitableExecutionBots.get(0);
					return new CommandDetails(botTypeInfo.getBotTypeId(), new CommandInfo("-start", "Start",
							botTypeInfo.getDescription(), botTypeInfo.getInputs(), new String[0], botTypeInfo.getSearchInfo(), 0));
				}
			}
			throw new FeatureImplementationNotFoundException(featureId);
		}
	}
	
	protected Optional<CommandDetails> findCommandByFeature(BotTypeInfo botTypeInfo, String featureId) {
		for (CommandInfo c : botTypeInfo.getCommands()) {
			for (String f : c.getFeatures()) {
				if (f.equals(featureId)) {
					return Optional.of(new CommandDetails(botTypeInfo.getBotTypeId(), c));
				}
			}
		}
		return Optional.empty();
	}
	
	protected CommandInfo getCommandById(BotTypeInfo botTypeInfo, String commandId) {
		for (CommandInfo c : botTypeInfo.getCommands()) {
			if (c.getCommandId().equals(commandId)) {
				return c;
			}
		}
		throw new UnexpectedException(String.format("Command '%s' not found in Bot Type '%s'", commandId, botTypeInfo.getBotTypeId()));
	}
	
	
	public static class CommandDetails {
		
		private final String hostRef;
		public String getHostRef() {
			return hostRef;
		}
		
		private final CommandInfo commandInfo;
		public CommandInfo getCommandInfo() {
			return commandInfo;
		}
		
		public CommandDetails(String hostRef, CommandInfo commandInfo) {
			this.hostRef = hostRef;
			this.commandInfo = commandInfo;
		}
	}
	
	public static class RunningBotDetails {
		
		private final String botId;
		public String getBotId() {
			return botId;
		}
		
		private final BotTypeInfo botTypeInfo;
		public BotTypeInfo getBotTypeInfo() {
			return botTypeInfo;
		}
		
		private final Map<String, Object> params;
		public Map<String, Object> getParams() {
			return params;
		}
		
		public RunningBotDetails(String botId, BotTypeInfo botTypeInfo, Map<String, Object> params) {
			this.botId = botId;
			this.botTypeInfo = botTypeInfo;
			this.params = params;
		}
	}
	
	public static class BotRefObject {
		
		private final String botId;
		public String getBotId() {
			return botId;
		}
		
		public BotRefObject(BotRef botRef) {
			this.botId = botRef.botId();
		}
	}
	
}
