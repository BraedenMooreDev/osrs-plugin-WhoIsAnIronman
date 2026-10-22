package org.BraedenMooreDev;

import net.runelite.client.config.*;


@ConfigGroup("Who is an Ironman")
public interface WhoIsAnIronmanConfig extends Config {

@ConfigSection(
		name = "Lookup",
		description = "Configure when player lookups are performed",
		position = 0,
		closedByDefault = false
)
String lookupSection = "lookupSection";

	//region Lookup

	@ConfigItem(
			keyName = "showMenuOption",
			name = "Player 'Who is' option",
			description = "Add option to players to perform lookup.",
			section = lookupSection,
			position = 0
	)
	default boolean showMenuOption() { return true; }

	@ConfigItem(
			keyName = "lookupOnRightClick",
			name = "Lookup on right click",
			description = "Perform lookup for all players on right clicked tile.",
			section = lookupSection,
			position = 1
	)
	default boolean lookupOnRightClick() { return false; }

	//endregion

@ConfigSection(
		name = "Chat information",
		description = "Configure how account types are displayed in chat when you click 'Who Is'.",
		position = 1,
		closedByDefault = false
)
String chatDisplaySection = "chatDisplaySection";

	//region ChatDisplay

	@ConfigItem(
			keyName = "showChat",
			name = "Show message in chat",
			description = "Show player's current account type in game chat.",
			section = chatDisplaySection,
			position = 0
	)
	default boolean showChat() { return true; }

	@ConfigItem(
			keyName = "showChatLost",
			name = "Previous account types",
			description = "Show player's previous account types in game chat.",
			section = chatDisplaySection,
			position = 2
	)
	default boolean showLostInChat() { return true; }

	//endregion

@ConfigSection(
		name = "Context menu helmets",
		description = "Configure how account types are displayed in right click context menu.",
		position = 2,
		closedByDefault = false
)
String menuDisplaySection = "menuDisplaySection";

	//region MenuDisplay

	@ConfigItem(
			keyName = "showMenu",
			name = "Helmets in right click menu",
			description = "Show player's current account type in right click context menu.",
			section = menuDisplaySection,
			position = 0
	)
	default boolean showMenu() { return true; }

	@ConfigItem(
			keyName = "showMenuLost",
			name = "Previous account types",
			description = "Show player's previous account types in right click context menu.",
			section = menuDisplaySection,
			position = 1
	)
	default boolean showLostInMenu() { return false; }

	//endregion
}