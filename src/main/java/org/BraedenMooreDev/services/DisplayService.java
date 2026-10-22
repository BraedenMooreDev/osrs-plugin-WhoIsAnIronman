package org.BraedenMooreDev.services;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.game.ChatIconManager;
import org.BraedenMooreDev.WhoIsAnIronmanConfig;
import org.BraedenMooreDev.models.AccountType;
import org.BraedenMooreDev.models.AccountTypeInfo;

import java.util.List;
import java.util.Set;

@Slf4j
@Singleton
public class DisplayService {

    private WhoIsAnIronmanConfig config;
    private Client client;
    private ChatMessageManager chatMessageManager;
    private ChatIconManager chatIconManager;

    @Inject
    public DisplayService(WhoIsAnIronmanConfig config, Client client, ChatMessageManager chatMessageManager, ChatIconManager chatIconManager) {
        this.config = config;
        this.client = client;
        this.chatMessageManager = chatMessageManager;
        this.chatIconManager = chatIconManager;
    }

    public void startUp() {
        for (AccountType type : AccountType.values())
        {
            log.debug("Loading icons for {}", type);

            try {
                type.loadIcons(client, chatIconManager);
            } catch(Exception e) {
                log.error("Failed loading icons for {}", type, e);
            }
        }
    }

    public void shutDown() {
        log.info("DisplayService shutdown");
    }

    public void showMenuLoading(MenuEntry entry) {
        String target = entry.getTarget();

        if (!config.showMenu() || target.startsWith("<img=") || target.startsWith("<col=ffaa00>?</col> ")) {
            return;
        }

        entry.setTarget("<col=ffaa00>?</col> " + target);
    }

    public void showMenuHelmets(MenuEntry entry, List<AccountTypeInfo> types) {
        String target = entry.getTarget();

        if (!config.showMenu() || target.startsWith("<img=")) {
            return;
        }

        if (types == null || types.isEmpty()) {
            return;
        }

        StringBuilder prefix = new StringBuilder()
                .append(typeIconTag(types.get(0).type, false));

        if (config.showLostInMenu() && types.size() > 1) {
            for (AccountTypeInfo typeInfo : types.subList(1, types.size())) {
                prefix.append(typeIconTag(typeInfo.type, true));
            }
        }

        for(AccountTypeInfo typeInfo : types) {
            log.debug("{}, {}", typeInfo.type, typeIconTag(typeInfo.type, !typeInfo.equals(types.get(0))));
        }

        if (prefix.length() > 0) {
            entry.setTarget(prefix + target);
        }
    }

    public void showChat(String name, List<AccountTypeInfo> types)
    {
        if (!config.showChat() || types.isEmpty()) {
            return;
        }

        StringBuilder prefix = new StringBuilder()
            .append(typeIconTag(types.get(0).type, false));

        StringBuilder msgBuilder = new StringBuilder()
            .append(name.replace("<col=ffaa00>?</col> ", ""))
            .append("</col> is ")
            .append(phraseTypeForChat(types.get(0)));

        if ((config.showLostInMenu() || config.showLostInChat()) && types.size() > 1) {
            for (AccountTypeInfo typeInfo : types.subList(1, types.size())) {
                if (typeInfo.type == AccountType.NORMAL)
                    continue;

                if (config.showLostInMenu())
                    prefix.append(typeIconTag(typeInfo.type, true));

                if (config.showLostInChat())
                    msgBuilder.append(" Was ").append(phraseTypeForChat(typeInfo));
            }
        }

        if (prefix.length() > 0)
            msgBuilder.insert(  0, prefix);

        msgBuilder.insert(0, "<col=ffaa00>Who Is:</col> ");

        chatMessageManager.queue(
            QueuedMessage.builder()
                .type(ChatMessageType.PLAYERRELATED)
                .value(msgBuilder.toString())
                .build()
        );
    }

    private StringBuilder typeIconTag(AccountType type, boolean lost) {
        if (!lost && type.getIcon() <= 0)
            return new StringBuilder();
        if (lost && type.getLostIcon() <= 0)
            return new StringBuilder();

        return new StringBuilder().append("<img=").append(lost ? type.getLostIcon() : type.getIcon()).append(">");
    }

    private StringBuilder phraseTypeForChat(AccountTypeInfo type) {

        StringBuilder msgBuilder = new StringBuilder();
        String typeStr = type.toString();

        if (startsWithVowel(typeStr))
            msgBuilder.append("an");
        else
            msgBuilder.append("a");

        msgBuilder.append(" <col=E6C35C>")
            .append(typeStr)
            .append("</col> at Total Level: <col=CCFFCC>")
            .append(type.skill.getLevel())
            .append("</col>.");

        return msgBuilder;
    }

    public void debugChatIcons(int startId, int count)
    {
        for (int id = startId; id < startId + count; id++)
        {
            String message = "Icon ID " + id + ": <img=" + id + ">";
            chatMessageManager.queue(
                    QueuedMessage.builder()
                            .type(ChatMessageType.CONSOLE)
                            .value(message)
                            .build()
            );
        }
    }

    Set<Character> vowels = Set.of('i', 'a', 'e', 'o', 'u');

    private boolean startsWithVowel(String str) {
        return vowels.contains(str.toLowerCase().charAt(0));
    }
}
