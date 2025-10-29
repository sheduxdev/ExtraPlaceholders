package dev.shedux.extraplaceholders.handler;

import dev.shedux.extraplaceholders.config.Configuration;
import dev.shedux.extraplaceholders.core.Initializer;
import dev.shedux.extraplaceholders.tracker.BoltTracker;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import xyz.refinedev.practice.api.kit.IKit;
import xyz.refinedev.practice.api.match.IMatch;
import xyz.refinedev.practice.api.match.MatchState;
import xyz.refinedev.practice.api.match.meta.IMatchPlayer;
import xyz.refinedev.practice.api.match.meta.IMatchTeam;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Handles Bolt-related placeholders for match and kit information
 * Provides comprehensive match tracking and kit rule validation
 *
 * @author sheduxdev
 * @since 1.0.0
 */
public final class BoltPlaceholderHandler implements PlaceholderHandler {

    private static final String TRUE = "true";
    private static final String FALSE = "false";
    private static final String KIT_COMMAND = "kit";
    private static final String MATCH_COMMAND = "match";
    private static final String WINNER_COMMAND = "winner";
    private static final String LOSER_COMMAND = "loser";
    private static final String RULE_COMMAND = "rule";

    private static final int MIN_ARGS = 2;
    private static final int COMMAND_INDEX = 1;
    private static final int SUBCOMMAND_INDEX = 2;
    private static final int KIT_NAME_INDEX = 2;
    private static final int RULE_NAME_INDEX = 3;
    private static final int SPECIFIC_KIT_RULE_INDEX = 4;

    private static Map<String, Function<IKit, Boolean>> kitRules;

    @Override
    public String handle(OfflinePlayer player, List<String> args) {
        BoltTracker tracker = Initializer.getBolt();

        if (!tracker.isApiAvailable()) {
            return Configuration.MESSAGES.BOLT_NOT_AVAILABLE;
        }

        if (!hasMinimumArgs(args, MIN_ARGS)) {
            return null;
        }

        String command = args.get(COMMAND_INDEX).toLowerCase();

        return switch (command) {
            case KIT_COMMAND -> handleKitPlaceholder(player, args, tracker);
            case MATCH_COMMAND -> handleMatchPlaceholder(player, args, tracker);
            default -> null;
        };
    }

    /**
     * Handles match-related placeholders (winner/loser)
     *
     * @param player the player
     * @param args the placeholder arguments
     * @param tracker the Bolt tracker
     * @return formatted match result
     */
    private String handleMatchPlaceholder(OfflinePlayer player, List<String> args, BoltTracker tracker) {
        if (!hasMinimumArgs(args, 3) || !(player instanceof Player onlinePlayer)) {
            return Configuration.MESSAGES.BOLT_NOT_AVAILABLE;
        }

        String subCommand = args.get(SUBCOMMAND_INDEX).toLowerCase();

        return switch (subCommand) {
            case WINNER_COMMAND -> handleMatchWinner(onlinePlayer, tracker);
            case LOSER_COMMAND -> handleMatchLoser(onlinePlayer, tracker);
            default -> null;
        };
    }

    /**
     * Handles kit-related placeholders
     *
     * @param player the player
     * @param args the placeholder arguments
     * @param tracker the Bolt tracker
     * @return formatted kit information
     */
    private String handleKitPlaceholder(OfflinePlayer player, List<String> args, BoltTracker tracker) {
        if (!(player instanceof Player onlinePlayer)) {
            return Configuration.MESSAGES.KIT_DEFAULT;
        }

        IMatch match = getPlayerMatch(onlinePlayer, tracker);
        if (match == null) {
            return Configuration.MESSAGES.KIT_OUT_OF_MATCH;
        }

        IKit currentKit = match.getKit();
        if (currentKit == null) {
            return Configuration.MESSAGES.KIT_LOADING;
        }

        if (isCurrentKitRuleCheck(args)) {
            return handleCurrentKitRule(args, currentKit);
        }

        if (isSpecificKitRuleCheck(args)) {
            return handleSpecificKitRule(args, tracker);
        }

        return null;
    }

    /**
     * Handles winner placeholder for all match types
     *
     * @param player the player
     * @param tracker the Bolt tracker
     * @return formatted winner information
     */
    private String handleMatchWinner(Player player, BoltTracker tracker) {
        return getMatchResult(player, tracker, new WinnerMatchResolver());
    }

    /**
     * Handles loser placeholder for all match types
     *
     * @param player the player
     * @param tracker the Bolt tracker
     * @return formatted loser information
     */
    private String handleMatchLoser(Player player, BoltTracker tracker) {
        return getMatchResult(player, tracker, new LoserMatchResolver());
    }

    /**
     * Generic method to get match results based on resolver strategy
     *
     * @param player the player
     * @param tracker the Bolt tracker
     * @param resolver the match result resolver
     * @return formatted match result
     */
    private String getMatchResult(Player player, BoltTracker tracker, MatchResultResolver resolver) {
        IMatch match = getPlayerMatch(player, tracker);

        if (!isMatchEnding(match)) {
            return Configuration.MESSAGES.BOLT_NOT_AVAILABLE;
        }

        if (match.isSoloMatch()) {
            return resolver.resolveSolo(match)
                    .map(this::getPlayerName)
                    .orElse(Configuration.MESSAGES.BOLT_NOT_AVAILABLE);
        }

        if (match.isTeamMatch()) {
            return resolver.resolveTeam(match)
                    .map(this::formatTeamNames)
                    .orElse(Configuration.MESSAGES.BOLT_NOT_AVAILABLE);
        }

        if (match.isFFAMatch()) {
            return resolver.resolveFFA(match)
                    .orElse(Configuration.MESSAGES.BOLT_NOT_AVAILABLE);
        }

        return Configuration.MESSAGES.BOLT_NOT_AVAILABLE;
    }

    /**
     * Handles current kit rule checking
     *
     * @param args the placeholder arguments
     * @param currentKit the current kit
     * @return "true" or "false"
     */
    private String handleCurrentKitRule(List<String> args, IKit currentKit) {
        if (!hasMinimumArgs(args, 4)) {
            return null;
        }

        String ruleName = args.get(RULE_NAME_INDEX).toLowerCase();
        return checkKitRule(currentKit, ruleName) ? TRUE : FALSE;
    }

    /**
     * Handles specific kit rule checking
     *
     * @param args the placeholder arguments
     * @param tracker the Bolt tracker
     * @return "true" or "false"
     */
    private String handleSpecificKitRule(List<String> args, BoltTracker tracker) {
        if (!hasMinimumArgs(args, 5)) {
            return null;
        }

        String kitName = args.get(KIT_NAME_INDEX);
        String ruleName = args.get(SPECIFIC_KIT_RULE_INDEX).toLowerCase();

        IKit kit = getKitByName(tracker, kitName);
        if (kit == null) {
            return Configuration.MESSAGES.KIT_INVALID;
        }

        return checkKitRule(kit, ruleName) ? TRUE : FALSE;
    }

    /**
     * Checks if a kit has a specific rule enabled
     *
     * @param kit the kit to check
     * @param ruleName the rule name
     * @return true if rule is enabled
     */
    private boolean checkKitRule(IKit kit, String ruleName) {
        if (kitRules == null) {
            kitRules = createKitRulesMap();
        }

        return Optional.ofNullable(kitRules.get(ruleName))
                .map(rule -> rule.apply(kit))
                .orElse(false);
    }

    /**
     * Gets player's current match safely
     *
     * @param player the player
     * @param tracker the Bolt tracker
     * @return the match or null
     */
    private IMatch getPlayerMatch(Player player, BoltTracker tracker) {
        try {
            return tracker.getApi().getMatchAPI().getMatchByPlayer(player);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets kit by name safely
     *
     * @param tracker the Bolt tracker
     * @param kitName the kit name
     * @return the kit or null
     */
    private IKit getKitByName(BoltTracker tracker, String kitName) {
        try {
            return tracker.getApi().getKitAPI().getKit(kitName);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Checks if match is in ending state
     *
     * @param match the match to check
     * @return true if match is ending
     */
    private boolean isMatchEnding(IMatch match) {
        return match != null && match.getState() == MatchState.ENDING;
    }

    /**
     * Checks if args represent current kit rule check
     *
     * @param args the arguments
     * @return true if checking current kit rule
     */
    private boolean isCurrentKitRuleCheck(List<String> args) {
        return hasMinimumArgs(args, 3) && RULE_COMMAND.equalsIgnoreCase(args.get(SUBCOMMAND_INDEX));
    }

    /**
     * Checks if args represent specific kit rule check
     *
     * @param args the arguments
     * @return true if checking specific kit rule
     */
    private boolean isSpecificKitRuleCheck(List<String> args) {
        return hasMinimumArgs(args, 4) && RULE_COMMAND.equalsIgnoreCase(args.get(RULE_NAME_INDEX));
    }

    /**
     * Gets player name safely
     *
     * @param matchPlayer the match player
     * @return player name or error message
     */
    private String getPlayerName(IMatchPlayer matchPlayer) {
        return Optional.ofNullable(matchPlayer.getPlayer())
                .map(Player::getName)
                .orElse(Configuration.MESSAGES.BOLT_NOT_AVAILABLE);
    }

    /**
     * Formats team player names as comma-separated string
     *
     * @param team the team
     * @return formatted player names
     */
    private String formatTeamNames(IMatchTeam team) {
        List<Player> players = team.getPlayers();
        if (players.isEmpty()) {
            return Configuration.MESSAGES.BOLT_NOT_AVAILABLE;
        }
        return players.stream()
                .map(Player::getName)
                .collect(Collectors.joining(", "));
    }

    /**
     * Formats match player names as comma-separated string
     *
     * @param matchPlayers the match players
     * @return formatted player names
     */
    private String formatPlayerNames(List<IMatchPlayer> matchPlayers) {
        if (matchPlayers.isEmpty()) {
            return Configuration.MESSAGES.BOLT_NOT_AVAILABLE;
        }
        return matchPlayers.stream()
                .map(IMatchPlayer::getPlayer)
                .filter(Objects::nonNull)
                .map(Player::getName)
                .collect(Collectors.joining(", "));
    }

    /**
     * Finds the winner in a solo match
     *
     * @param match the match
     * @return Optional containing the winner
     */
    private Optional<IMatchPlayer> findWinnerInSolo(IMatch match) {
        for (Player p : match.getPlayers()) {
            IMatchPlayer mp = match.getMatchPlayer(p);
            if (mp != null && mp.isAlive()) {
                return Optional.of(mp);
            }
        }

        return match.getPlayers().stream()
                .map(match::getMatchPlayer)
                .filter(Objects::nonNull)
                .max(Comparator.comparingInt(IMatchPlayer::getPoints));
    }

    /**
     * Finds the loser in a solo match
     *
     * @param match the match
     * @return Optional containing the loser
     */
    private Optional<IMatchPlayer> findLoserInSolo(IMatch match) {
        return findWinnerInSolo(match).flatMap(winner ->
                match.getPlayers().stream()
                        .map(match::getMatchPlayer)
                        .filter(mp -> mp != null && mp != winner)
                        .findFirst()
        );
    }

    /**
     * Finds the winner team
     *
     * @param match the match
     * @return Optional containing the winner team
     */
    private Optional<IMatchTeam> findWinnerTeam(IMatch match) {
        List<IMatchTeam> teams = match.getPlayers().stream()
                .map(match::getMatchTeam)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (teams.size() != 2) {
            return teams.isEmpty() ? Optional.empty() : Optional.of(teams.get(0));
        }

        IMatchTeam team1 = teams.get(0);
        IMatchTeam team2 = teams.get(1);

        if (team1.getAliveCount() != team2.getAliveCount()) {
            return Optional.of(team1.getAliveCount() > team2.getAliveCount() ? team1 : team2);
        }

        return Optional.of(team1.getPoints() >= team2.getPoints() ? team1 : team2);
    }

    /**
     * Finds the loser team
     *
     * @param match the match
     * @return Optional containing the loser team
     */
    private Optional<IMatchTeam> findLoserTeam(IMatch match) {
        return findWinnerTeam(match).flatMap(winner ->
                match.getPlayers().stream()
                        .map(match::getMatchTeam)
                        .filter(team -> team != null && team != winner)
                        .findFirst()
        );
    }

    /**
     * Finds the winner in an FFA match
     *
     * @param match the match
     * @return Optional containing the winner
     */
    private Optional<IMatchPlayer> findWinnerInFFA(IMatch match) {
        return match.getPlayers().stream()
                .map(match::getMatchPlayer)
                .filter(mp -> mp != null && mp.isAlive())
                .max(Comparator.comparingInt(IMatchPlayer::getPoints))
                .or(() -> match.getPlayers().stream()
                        .map(match::getMatchPlayer)
                        .filter(Objects::nonNull)
                        .max(Comparator.comparingInt(IMatchPlayer::getPoints))
                );
    }

    /**
     * Finds all losers in an FFA match
     *
     * @param match the match
     * @return list of losers
     */
    private List<IMatchPlayer> findLosersInFFA(IMatch match) {
        Optional<IMatchPlayer> winner = findWinnerInFFA(match);
        return match.getPlayers().stream()
                .map(match::getMatchPlayer)
                .filter(mp -> mp != null && !winner.map(w -> w == mp).orElse(false))
                .collect(Collectors.toList());
    }

    /**
     * Creates the kit rules mapping (lazy initialization)
     *
     * @return immutable map of rule names to checker functions
     */
    private static Map<String, Function<IKit, Boolean>> createKitRulesMap() {
        return Map.ofEntries(
                Map.entry("enabled", IKit::isEnabled),
                Map.entry("ranked", IKit::isRanked),
                Map.entry("build", IKit::isBuild),
                Map.entry("showhp", IKit::isShowHP),
                Map.entry("spleef", IKit::isSpleef),
                Map.entry("battlerush", IKit::isBattleRush),
                Map.entry("fireballfight", IKit::isFireballFight),
                Map.entry("pearlfight", IKit::isPearlFight),
                Map.entry("bridges", IKit::isBridges),
                Map.entry("pearldamage", IKit::isPearlDamage),
                Map.entry("nodrop", IKit::isNoDrop),
                Map.entry("noregen", IKit::isNoRegen),
                Map.entry("nofall", IKit::isNoFall),
                Map.entry("nohunger", IKit::isNoHunger),
                Map.entry("blockremoval", IKit::isBlockRemoval),
                Map.entry("respawnmode", IKit::isRespawnMode),
                Map.entry("legacycombat", IKit::isLegacyCombat),
                Map.entry("buildheightdamage", IKit::isBuildHeightDamage),
                Map.entry("topfight", IKit::isTopFight),
                Map.entry("bedfight", IKit::isBedFight),
                Map.entry("stickfight", IKit::isStickFight),
                Map.entry("stickspawn", IKit::isStickSpawn),
                Map.entry("partyffa", IKit::isPartyFFA),
                Map.entry("partysplit", IKit::isPartySplit),
                Map.entry("voidspawn", IKit::isVoidSpawn),
                Map.entry("boxing", IKit::isBoxing),
                Map.entry("combo", IKit::isCombo),
                Map.entry("sumo", IKit::isSumo),
                Map.entry("liquidkill", IKit::isLiquidKill),
                Map.entry("mlgrush", IKit::isMlgRush),
                Map.entry("crystalpvp", IKit::isCrystalPvP),
                Map.entry("cartpvp", IKit::isCartPvP),
                Map.entry("tntsumo", IKit::isTntSumo),
                Map.entry("windchargemode", IKit::isWindChargeMode),
                Map.entry("oitq", IKit::isOitq),
                Map.entry("presplash", IKit::isPreSplash),
                Map.entry("breakmap", IKit::isBreakMap),
                Map.entry("pearlcooldown", IKit::isPearlCooldown),
                Map.entry("editable", IKit::isEditable),
                Map.entry("ffa", IKit::isFFA),
                Map.entry("portal", IKit::isPortal)
        );
    }

    /**
     * Strategy interface for resolving match results
     */
    private interface MatchResultResolver {
        Optional<IMatchPlayer> resolveSolo(IMatch match);
        Optional<IMatchTeam> resolveTeam(IMatch match);
        Optional<String> resolveFFA(IMatch match);
    }

    /**
     * Resolver for match winners
     */
    private class WinnerMatchResolver implements MatchResultResolver {
        @Override
        public Optional<IMatchPlayer> resolveSolo(IMatch match) {
            return findWinnerInSolo(match);
        }

        @Override
        public Optional<IMatchTeam> resolveTeam(IMatch match) {
            return findWinnerTeam(match);
        }

        @Override
        public Optional<String> resolveFFA(IMatch match) {
            return findWinnerInFFA(match).map(BoltPlaceholderHandler.this::getPlayerName);
        }
    }

    /**
     * Resolver for match losers
     */
    private class LoserMatchResolver implements MatchResultResolver {
        @Override
        public Optional<IMatchPlayer> resolveSolo(IMatch match) {
            return findLoserInSolo(match);
        }

        @Override
        public Optional<IMatchTeam> resolveTeam(IMatch match) {
            return findLoserTeam(match);
        }

        @Override
        public Optional<String> resolveFFA(IMatch match) {
            List<IMatchPlayer> losers = findLosersInFFA(match);
            return losers.isEmpty() ? Optional.empty() : Optional.of(formatPlayerNames(losers));
        }
    }
}