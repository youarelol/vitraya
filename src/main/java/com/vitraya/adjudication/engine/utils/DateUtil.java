package com.vitraya.adjudication.engine.utils;

import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class DateUtil {
    private static final String DEFAULT_FORMAT = "yyyy-MM-dd";
    private static final String NIVA_DATE_FORMAT = "dd-MMM-yyyy";

    // List of supported date formats
    private static final List<String> SUPPORTED_FORMATS = new ArrayList<>();

    static {
        SUPPORTED_FORMATS.add(DEFAULT_FORMAT); // Default format
        SUPPORTED_FORMATS.add("MMM dd, yyyy hh:mm:ss a"); // Example: "Jan 04, 1949 12:00:00 AM"
        SUPPORTED_FORMATS.add("yyyy-MM-dd HH:mm:ss"); // Example: "2024-12-24 18:30:00"
        SUPPORTED_FORMATS.add("yyyy-MM-dd HH:mm"); // Example: "2024-12-24 18:30"
        SUPPORTED_FORMATS.add("yyyy-MM-dd"); // Example: "2024-12-24"
        // SUPPORTED_FORMATS.add("MM-dd-yyyy"); // Example: "12-24-2024"
    }

    // 1. Parse Date to String with default format
    public static String dateToString(Date date) {
        return new SimpleDateFormat(DEFAULT_FORMAT).format(date);
    }

    // 2. Parse Date to String with custom format
    public static String dateToString(Date date, String format) {
        return new SimpleDateFormat(format).format(date);
    }

    // 3. Parse String to Date with default format
    public static Date stringToDate(String dateString) throws ParseException {
        if (dateString == null) {
            return null;
        }
        return stringToDateWithFormats(dateString);
    }

    // 4. Parse String to Date with custom format
    public static Date stringToDate(String dateString, String format) throws ParseException {
        return new SimpleDateFormat(format).parse(dateString);
    }

    // 5. Parse String to Date with multiple formats
    public static Date stringToDateWithFormats(String dateString) throws ParseException {
        if (dateString == null) {
            return null;
        }
        for (String format : SUPPORTED_FORMATS) {
            try {
                return new SimpleDateFormat(format, Locale.ENGLISH).parse(dateString);
            } catch (ParseException ignored) {
                // Try the next format
            }
        }
        throw new ParseException("Unable to parse date: " + dateString, 0);
    }

    /**
     * Get a past or future date based on the given number of days.
     *
     * @param date The reference date
     * @param days Number of days to add (positive for future, negative for past)
     * @return The calculated date
     */
    public static Date getPastOrFutureDate(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date); // Set the given date
        calendar.add(Calendar.DAY_OF_MONTH, days); // Add/subtract days
        return calendar.getTime();
    }

    /**
     * Get a past or future date based on the given minutes.
     *
     * @param date The reference date
     * @param days Number of days to add (positive for future, negative for past)
     * @return The calculated date
     */
    public static Date getPastOrFutureDateMinuteBased(Date date, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date); // Set the given date
        calendar.add(Calendar.MINUTE, minutes); // Add/subtract days
        return calendar.getTime();
    }

    public static LocalDateTime getPastOrFutureDateTime(LocalDateTime dateTime, int days) {
        return dateTime.plusDays(days); // Use negative days for past dates
    }

    // 7. Get difference in days between two dates
    public static long getDifferenceInDays(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    // 8. Get difference in months between two dates
    public static long getDifferenceInMonths(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.MONTHS.between(startDate, endDate);
    }

    // 9. Get difference in years between two dates
    public static long getDifferenceInYears(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.YEARS.between(startDate, endDate);
    }

    public static int calculateAge(String dateObj) throws ParseException {
        Date dateOfBirth = stringToDateWithFormats(dateObj); // Updated to use multiple formats
        return calculateAge(dateOfBirth);
    }

    // Calculate age based on date of birth (LocalDate)
    public static int calculateAge(LocalDate dateOfBirth) {
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    // Overloaded method for Date input
    public static int calculateAge(Date dateOfBirth) {
        if (dateOfBirth != null) {
            LocalDate dob = dateOfBirth.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            return calculateAge(dob);
        }

        return 0;
    }

    public static int getTimeDifference(Date startDate, Date endDate) {
        return (int) ((endDate.getTime() - startDate.getTime()));
    }

    public static String parseDateInNivaFormat(Date admissionDate) {
        return new SimpleDateFormat(NIVA_DATE_FORMAT).format(admissionDate);
    }

    public static Date getAdjustedDate(int claimCheckTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -claimCheckTime);
        return calendar.getTime();
    }
}
