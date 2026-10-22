package org.BraedenMooreDev;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

import net.runelite.api.*;
import net.runelite.api.MenuAction;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.plugins.PluginManager;
import org.BraedenMooreDev.services.DisplayService;
import org.BraedenMooreDev.services.LookupService;

import java.util.*;

@Slf4j
@PluginDescriptor(
		name = "Who is an Ironman",
		description = "Displays Ironman helmet icons next to player names in the right-click context menu."
)
public class WhoIsAnIronmanPlugin extends Plugin
{
	@Inject
	private WhoIsAnIronmanConfig config;

	@Inject
	private LookupService lookupService;

	@Inject
	private DisplayService displayService;

	@Inject
	private MenuManager menuManager;

	@Inject
	private PluginManager pluginManager;

	@Inject
	private ConfigManager configManager;

	private boolean hiscorelookupOptionAvailable;

	private static final String MENU_OPTION = "Who Is";

	@Override
	protected void startUp() {
		lookupService.startUp();
		displayService.startUp();

		updateLookupMenuOption();
	}

	@Override
	protected void shutDown() {
		menuManager.removePlayerMenuItem(MENU_OPTION);

		lookupService.shutDown();
		displayService.shutDown();
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event) {
		if (!config.showMenuOption())
			return;

		if (event.getMenuAction() != MenuAction.RUNELITE_PLAYER)
			return;

		if (!MENU_OPTION.equals(event.getMenuOption()))
			return;

		Map<String, String> pendingPlayers = new HashMap<>();

		preparePlayerLookup(event.getMenuEntry(), pendingPlayers, true);
		executePendingLookups(pendingPlayers, true);
	}

	@Subscribe
	public void onMenuOpened(MenuOpened event)  {
		Map<String, String> pendingPlayers = new HashMap<>();

		for (MenuEntry entry : event.getMenuEntries()) {
			preparePlayerLookup(entry, pendingPlayers, false);
		}

		if (config.lookupOnRightClick())
			executePendingLookups(pendingPlayers, false);
	}

	private void preparePlayerLookup(MenuEntry entry, Map<String, String> pendingPlayers, boolean showInChat) {
		if (entry.getPlayer() == null)
			return;

		String rawName = entry.getTarget();
		String name = cleanName(rawName);

		if (name.isEmpty())
			return;

		lookupService.lookupCache(name).ifPresentOrElse(
				types -> { // If Present
					displayService.showMenuHelmets(entry, types);
					if (showInChat)
						displayService.showChat(rawName, types);
				},
				() -> { // Else
					boolean inProgress = lookupService.inProgress(name);

					if (config.lookupOnRightClick() || inProgress)
						displayService.showMenuLoading(entry);

					if (!inProgress || showInChat)
						pendingPlayers.put(name.toLowerCase(), rawName);
				}
		);
	}

	private void executePendingLookups(Map<String, String> pendingPlayers, boolean showInChat)
	{
		for (Map.Entry<String, String> player : pendingPlayers.entrySet()) {
			String rawName = player.getValue();
			String name = cleanName(rawName);

			lookupService.lookup(name).thenAccept(types -> {
				if (showInChat)
					displayService.showChat(rawName, types);
			});
		}
	}

	private void updateLookupMenuOption()
	{
		if (config.showMenuOption())
		{
			menuManager.addPlayerMenuItem(MENU_OPTION);
		}
		else
		{
			menuManager.removePlayerMenuItem(MENU_OPTION);
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if ("Who is an Ironman".equals(event.getGroup())
				&& "showMenuOption".equals(event.getKey()))
		{
			updateLookupMenuOption();
		}
	}

	private String cleanName(String target) {
		if (target == null)
			return "";

		return target.replace('\u00A0', ' ')
				.replaceAll("<[^>]*>", "")
				.replaceAll("\\(.*?\\)", "")
				.trim();
	}

	@Provides
    WhoIsAnIronmanConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(WhoIsAnIronmanConfig.class);
	}
}