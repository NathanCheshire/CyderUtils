package com.github.natche.cyderutils.time;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Immutable;

import java.time.LocalDate;
import java.util.Calendar;

/** A class used to represent a month and date such as July 4th. */
@Immutable
@SuppressWarnings("ClassCanBeRecord")
public final class MonthDay {
    /** The calendar instance for querying today's month and date. */
    private static final Calendar calendarInstance = Calendar.getInstance();

    /** The today month day object. */
    public static final MonthDay TODAY = new MonthDay(
            calendarInstance.get(Calendar.MONTH) + 1,
            calendarInstance.get(Calendar.DATE));

    /** The "the" string used by the {@link #getMonthDateString()} method. */
    private static final String THE = "the";

    /** The month number. */
    private final int month;

    /** The date number. */
    private final int date;

    /**
     * Constructs a new month day object.
     *
     * @param month the month number starting at 1 for January
     * @param date  the date of the month
     */
    public MonthDay(int month, int date) {
        Preconditions.checkArgument(month > 0 && month <= 12);
        Preconditions.checkArgument(date > 0 && date <= 31);

        this.month = month;
        this.date = date;
    }

    /**
     * Returns a new {@link MonthDay} object from the provided LocalDate.
     *
     * @param date the local date
     * @return a new month day object
     */
    public static MonthDay fromDate(LocalDate date) {
        Preconditions.checkNotNull(date);

        return new MonthDay(date.getMonthValue(), date.getDayOfMonth());
    }

    /**
     * Returns the month number.
     *
     * @return the month number
     */
    public int getMonth() {
        return month;
    }

    /**
     * Returns the date number.
     *
     * @return the date number
     */
    public int getDate() {
        return date;
    }

    /**
     * Returns the month String for the month.
     *
     * @return the month String for the month.
     */
    public String getMonthString() {
        return TimeUtil.months.get(month);
    }

    /**
     * Returns the date String for the month.
     * For example, if date were "5", "5th" would be returned.
     *
     * @return the date String for the month
     */
    public String getDateString() {
        return TimeUtil.formatNumberSuffix(date);
    }

    /**
     * Returns the month date string. This is the result of concatenating
     * {@link #getMonthString()}, " and ", and {@link #getDateString()}.
     *
     * @return the month date string
     */
    public String getMonthDateString() {
        return getMonthString() + " " + THE + " " + getDateString();
    }

    /**
     * Returns whether today is the provided special day.
     *
     * @param specialDay the special day to compare to today
     * @return whether today is the provided special day
     */
    public boolean isSpecialDay(SpecialDay specialDay) {
        Preconditions.checkNotNull(specialDay);

        return TimeUtil.isSpecialDay(this, specialDay);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "MonthDay{"
                + "month=" + month
                + ", date=" + date
                + "}";
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int ret = Integer.hashCode(month);
        ret = 31 * ret + Integer.hashCode(date);
        return ret;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof MonthDay)) {
            return false;
        }

        MonthDay other = (MonthDay) o;
        return other.date == date
                && other.month == month;
    }
}
