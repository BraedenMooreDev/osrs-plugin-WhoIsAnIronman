package org.BraedenMooreDev.services;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.hiscore.HiscoreEndpoint;
import net.runelite.client.hiscore.HiscoreManager;
import net.runelite.client.hiscore.HiscoreResult;
import net.runelite.client.hiscore.HiscoreSkill;
import org.BraedenMooreDev.WhoIsAnIronmanConfig;
import org.BraedenMooreDev.models.AccountType;
import org.BraedenMooreDev.models.AccountTypeInfo;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Singleton
public class LookupService {

    private static final int THREAD_COUNT = 8;

    private WhoIsAnIronmanConfig config;
    private HiscoreManager hiscoreManager;
    private ExecutorService lookupExecutor;

    private final Map<String, List<AccountTypeInfo>> typesCache = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<List<AccountTypeInfo>>> pendingLookups = new ConcurrentHashMap<>();

    private static final List<HiscoreEndpoint> IRONMAN_LOOKUP_PRIORITY = Arrays.asList(
            HiscoreEndpoint.NORMAL,
            HiscoreEndpoint.IRONMAN,
            HiscoreEndpoint.HARDCORE_IRONMAN,
            HiscoreEndpoint.ULTIMATE_IRONMAN
    );

    @Inject
    public LookupService(WhoIsAnIronmanConfig config, HiscoreManager hiscoreManager) {
        this.config = config;
        this.hiscoreManager = hiscoreManager;
    }

    public void startUp() {
        lookupExecutor = Executors.newFixedThreadPool(THREAD_COUNT);
    }

    public void shutDown() {
        log.info("LookupService shutdown");

        if (lookupExecutor != null) {
            lookupExecutor.shutdownNow();
        }

        pendingLookups.clear();
    }

    public Optional<List<AccountTypeInfo>> lookupCache(String name) {

        String key = name.toLowerCase();

        List<AccountTypeInfo> types = typesCache.get(key);

        if (types == null) {
            return Optional.empty();
        }

        return Optional.of(List.copyOf(types));
    }

    public boolean inProgress(String name) {
        String key = name.toLowerCase();
        return pendingLookups.containsKey(key);
    }

    public CompletableFuture<List<AccountTypeInfo>> lookup(String name) {

        String key = name.toLowerCase();

        return pendingLookups.computeIfAbsent(key, k -> {

            log.info("Starting account lookup for {}", name);

            CompletableFuture<List<AccountTypeInfo>> future = CompletableFuture.supplyAsync(() ->
            {
                try {
                    List<AccountTypeInfo> types = lookupIronmanType(name);
                    typesCache.put(key, types);
                    log.info("Resolved {} -> {}", name, types);
                    return types;
                } catch (Exception e) {
                    log.error("Failed resolving account type for {}", name, e);
                    return List.of();
                }
            }, lookupExecutor);

            future.whenComplete((result, error) -> {
                pendingLookups.remove(key);
                log.debug("Lookup completed for {}", name);
            });

            return future;
        });
    }

    private List<AccountTypeInfo> lookupIronmanType(String name) {

        List<AccountTypeInfo> types = new ArrayList<>();
        long highestTotalExp = 0;

        for(HiscoreEndpoint endpoint : IRONMAN_LOOKUP_PRIORITY) {
            try {
                HiscoreResult result = hiscoreManager.lookup(name, endpoint);
                log.debug("Name: {}, Results: {}", name, result);
                if(result == null) {
                    continue;
                }

                AccountType type = endpointToType(endpoint);
                if (type == null) continue;

                net.runelite.client.hiscore.Skill overall = result.getSkill(HiscoreSkill.OVERALL);
                AccountTypeInfo typeInfo = new AccountTypeInfo(type, overall);

                log.debug("Hiscore lookup results. Player={}, Endpoint={}, TotalLevel={}", name, type, overall.getLevel());

                if (typeInfo.skill.getExperience() >= highestTotalExp) {
                    highestTotalExp = typeInfo.skill.getExperience();

                    if (types.isEmpty())
                        types.add(typeInfo);
                    else
                        types.set(0, typeInfo);
                } else {
                    types.add(typeInfo);
                }

            } catch(Exception ex) {
                log.warn("Hiscore lookup failed. Player={}, Endpoint={}", name, endpoint, ex);
                return List.of();
            }
        }

        return types;
    }

    private AccountType endpointToType(HiscoreEndpoint endpoint) {
        switch(endpoint) {
            case ULTIMATE_IRONMAN:
                return AccountType.ULTIMATE_IRONMAN;
            case HARDCORE_IRONMAN:
                return AccountType.HARDCORE_IRONMAN;
            case IRONMAN:
                return AccountType.IRONMAN;
            case NORMAL:
                return AccountType.NORMAL;
        }
        return null;
    }
}
