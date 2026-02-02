package org.unitedlands.politics.managers;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.classes.NewDayRunnable;
import org.unitedlands.utils.Logger;

public class TimeManager {

    private final UnitedPolitics plugin;

    private final Pattern TIME_PATTERN = Pattern.compile("(\\d+)([dhms])");

    private BukkitTask newDayTask = null;
    private long newDayExecutionTime; 

    public TimeManager(UnitedPolitics plugin) {
        this.plugin = plugin;
    }

    public void cancelScheduledNewDay() {
        if (newDayTask != null) {
            newDayTask.cancel();
            newDayTask = null;
        }
    }

    public void scheduleNewDay() {

        String interval = plugin.getConfig().getString("new-day-interval", "1d");
        int newDayHour = plugin.getConfig().getInt("new-day-start-hour", 12);

        long secondsToNewDay = 0;
        if (interval.equals("1d")) {
            secondsToNewDay = secondsUntilNextUtcHour(newDayHour);
        } else {
            secondsToNewDay = parseTimeToSeconds(interval);
        }

        Logger.log("Scheduling new day in " + secondsToNewDay + " seconds...", "UnitedPolitics");

        var newDayRunnable = new NewDayRunnable(plugin);
        var tickDelay = secondsToNewDay * 20;
        newDayExecutionTime = System.currentTimeMillis() + (secondsToNewDay * 1000);

        newDayTask = Bukkit.getScheduler().runTaskLater(plugin, newDayRunnable, tickDelay);

    }

    public long getTimeToNewDay() {
        return Math.max(0, newDayExecutionTime - System.currentTimeMillis());
    }
    
    public static long secondsUntilNextUtcHour(int targetHourUTC) {

        if (targetHourUTC < 0 || targetHourUTC > 23) {
            throw new IllegalArgumentException("Hour must be between 0 and 23");
        }

        Instant now = Instant.now();
        ZonedDateTime nowUtc = now.atZone(ZoneOffset.UTC);

        ZonedDateTime target = nowUtc
                .withHour(targetHourUTC)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        // If the target hour is already reached or passed, move to next day
        if (!target.isAfter(nowUtc)) {
            target = target.plusDays(1);
        }

        return Duration.between(nowUtc, target).getSeconds();
    }

    public long parseTimeToSeconds(String input) {

        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Time string must not be null or empty");
        }

        Matcher matcher = TIME_PATTERN.matcher(input);

        long seconds = 0;
        int parsedLength = 0;

        while (matcher.find()) {
            long value = Long.parseLong(matcher.group(1));
            char unit = matcher.group(2).charAt(0);

            switch (unit) {
                case 'd' -> seconds += value * 86_400;
                case 'h' -> seconds += value * 3_600;
                case 'm' -> seconds += value * 60;
                case 's' -> seconds += value;
                default -> throw new IllegalStateException("Unexpected unit: " + unit);
            }

            parsedLength += matcher.group().length();
        }

        if (parsedLength != input.length()) {
            throw new IllegalArgumentException("Invalid time format: " + input);
        }

        return seconds;
    }

}
