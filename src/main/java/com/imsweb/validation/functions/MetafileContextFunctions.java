/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation.functions;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import groovy.lang.Binding;

import com.imsweb.staging.Staging;
import com.imsweb.validation.ValidationEngine;
import com.imsweb.validation.entities.ContextTable;
import com.imsweb.validation.entities.ContextTableIndex;

/**
 * Metafile-related helper methods made available to the edits. If you want to execute translated edits in your project, you need to initialize
 * the context functions with an instance of this class.
 * <br/><br/>
 * None of these methods should be called from regular (non-translated) edits.
 * <br/><br/>
 * As of version 5.3 of the engine, it is now possible to treat WARNINGS in translated edits as a failing state. To do that, just call
 * the setFailWarnings() method with an argument of "true" after instanciating this class.
 */
@SuppressWarnings("ALL")
public class MetafileContextFunctions extends StagingContextFunctions {

    // Special Geneedits constant
    public static final int DT_VALID = 1342177279;

    // Special Geneedits constant
    public static final int DT_MISSING = 1610612735;

    // Special Geneedits constant
    public static final int DT_ERROR = 1879048191;

    // Special Geneedits constant
    public static final int DT_UNKNOWN = 2147483647;

    // Special Geneedits constant
    public static final int DT_EMPTY = 1073741823;

    // Special Geneedits constant
    public static final int DT_DAY_EMPTY = 805306367;

    // Special Geneedits constant
    public static final int DT_MONTH_EMPTY = 536870911;

    // sentinel threshold, no sentinel value should be lower than this!
    public static final int SENTINEL_TRESHOLD = DT_MONTH_EMPTY;

    // Special Geneedits constant
    public static final int DT_MIN = 1;

    // Special Geneedits constant
    public static final int DT_MAX = 2;

    // Special Geneedits constant
    public static final int DT_EXACT = 3;

    // Special Geneedits constant
    public static final int TRIM_RIGHT = 0;

    // Special Geneedits constant
    public static final int TRIM_LEFT = 1;

    // Special Geneedits constant
    public static final int TRIM_BOTH = 2;

    // Special Geneedits constant
    public static final int JUSTIFIED_RIGHT = 0;

    // Special Geneedits constant
    public static final int JUSTIFIED_LEFT = 1;

    // Special genedits binding keys
    public static final String BINDING_KEY_FUTURE_DATE = "__years_into_future";

    // Special genedits binding keys
    public static final String BINDING_KEY_DATE_COMPONENT = "__date_component";

    // Pre-compiled regex and formatters...
    private static final Pattern _GEN_VAL_P1 = Pattern.compile("(-?\\d+)(.*)");
    private static final Pattern _GEN_VALID_DATE_IOP_P1 = Pattern.compile("(\\d{8}|\\d{6}\\s{2}|\\d{4}\\s{4})");
    private static final DateTimeFormatter _GEN_DATECMP_IOP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Pattern _GEN_TRIM_P1 = Pattern.compile("^\\s+");
    private static final Pattern _GEN_TRIM_P2 = Pattern.compile("\\s+$");
    private static final Pattern _GEN_TRIM_P3 = Pattern.compile("(^\\s+|\\s+$)");
    private static final Pattern _GEN_MATCH_P1 = Pattern.compile("\\s");
    private static final Pattern _GEN_MATCH_P2 = Pattern.compile("(\\\\s)+");
    private static final Pattern _GEN_MATCH_P3 = Pattern.compile("((\\\\s)+)\\)$");
    private static final Pattern _GEN_MATCH_P4 = Pattern.compile("((\\\\s)+)$");
    private static final Pattern _GEN_FMTSTR_P1 = Pattern.compile("%(.*)ld");
    private static final Pattern _GEN_INLIST_P1 = Pattern.compile("(\\d+)\\D*.*");

    // I don't love giving a state to this class, but I am not sure how else to do it since we already have multiple flavors for the constructor...
    private boolean _failWarnings = false;

    /**
     * Default constructor.
     */
    public MetafileContextFunctions() {
        super();
    }

    /**
     * Constructor.
     * @param csStaging <code>Staging</code> instance to use for Collaboriative-Stage-related operations.
     * @param tnmStaging <code>Staging</code> instance to use for TNM-related operations.
     */
    public MetafileContextFunctions(Staging csStaging, Staging tnmStaging) {
        super(csStaging, tnmStaging);
    }

    /**
     * If set to true, then warnings in translated edits will make the edit fail.
     */
    public void setFailWarnings(boolean failWarnings) {
        _failWarnings = failWarnings;
    }

    /**
     * @return whether warnings in translated edits are considered as failures.
     */
    public boolean isFailWarnings() {
        return _failWarnings;
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     */
    public void GEN_NOOP() {
        return;
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param binding
     */
    public void GEN_RESET_LOCAL_CONTEXT(Binding binding) {
        binding.setVariable(BINDING_KEY_FUTURE_DATE, null);
        binding.setVariable(BINDING_KEY_DATE_COMPONENT, null);
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param value
     * @return - internal use only -
     */
    public boolean GEN_EMPTY(Object value) {
        if (value == null)
            return true;

        return StringUtils.isBlank(GEN_TO_STRING(value));
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param value
     * @return - internal use only -
     */
    public int GEN_VAL(Object value) {
        int result = 0;

        String val = value == null ? "" : GEN_TO_STRING(value).trim();
        Matcher match = _GEN_VAL_P1.matcher(val);
        if (match.matches())
            result = Integer.valueOf(match.group(1));

        return result;
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param binding
     * @param value
     * @return - internal use only -
     */
    public boolean GEN_VALID_DATE_IOP(Binding binding, Object value) {
        String val = GEN_TO_STRING(value);

        if (val == null || val.isEmpty()) {
            binding.setVariable(BINDING_KEY_DATE_COMPONENT, "date is an empty string");
            return false;
        }

        if (!_GEN_VALID_DATE_IOP_P1.matcher(val).matches()) {
            binding.setVariable(BINDING_KEY_DATE_COMPONENT, "date format must be CCYYMMDD");
            return false;
        }

        LocalDate currentTime = LocalDate.now();
        int maxYear = currentTime.getYear();
        Integer futureYears = null;
        if (binding.hasVariable(BINDING_KEY_FUTURE_DATE))
            futureYears = (Integer)binding.getVariable(BINDING_KEY_FUTURE_DATE);
        if (futureYears != null)
            maxYear += futureYears;

        int year = Integer.valueOf(val.substring(0, 4));
        if (year < 1850 || year > maxYear) {
            binding.setVariable(BINDING_KEY_DATE_COMPONENT, "invalid as to year");
            return false;
        }

        int month = val.trim().length() >= 6 ? Integer.valueOf(val.substring(4, 6)) : 1;
        if (month <= 0 || month > 12) {
            binding.setVariable(BINDING_KEY_DATE_COMPONENT, "invalid as to month");
            return false;
        }

        try {
            int day = val.trim().length() == 8 ? Integer.valueOf(val.substring(6, 8)) : 1;
            LocalDate toCheck = LocalDate.of(year, month, day);
            int actualMaxDay = YearMonth.of(year, month).lengthOfMonth();
            if (day <= 0 || day > actualMaxDay) {
                binding.setVariable(BINDING_KEY_DATE_COMPONENT, "invalid as to day");
                return false;
            }

            if (futureYears != null)
                currentTime = currentTime.plusYears(futureYears);
            if (toCheck.compareTo(currentTime) > 0) {
                binding.setVariable(BINDING_KEY_DATE_COMPONENT, "future date");
                return false;
            }
        }
        catch (DateTimeException e) {
            return false; // Invalid date.  Like Feb 29th 2010.
        }

        return true;
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param binding
     * @param yearsIntoFuture
     */
    public void GEN_ALLOW_FUTURE_DATE_IOP(Binding binding, Object yearsIntoFuture) {
        binding.setVariable(BINDING_KEY_FUTURE_DATE, yearsIntoFuture);
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param binding
     * @param value
     * @return - internal use only -
     */
    public int GEN_DATE_YEAR_IOP(Binding binding, Object value) {
        String val = GEN_TO_STRING(value);

        if (val == null || val.trim().isEmpty())
            return DT_EMPTY;

        if (!GEN_VALID_DATE_IOP(binding, value))
            return DT_ERROR;

        return Integer.valueOf(val.substring(0, 4));
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param binding
     * @param value
     * @return - internal use only -
     */
    public int GEN_DATE_MONTH_IOP(Binding binding, Object value) {
        String val = GEN_TO_STRING(value);

        if (val == null || val.length() < 5 || (val.length() >= 6 && val.substring(4, 6).trim().isEmpty()))
            return DT_MONTH_EMPTY;

        if (!GEN_VALID_DATE_IOP(binding, value))
            return DT_ERROR;

        return Integer.valueOf(val.substring(4, 6));
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param binding
     * @param value
     * @return - internal use only -
     */
    public int GEN_DATE_DAY_IOP(Binding binding, Object value) {
        String val = GEN_TO_STRING(value);

        if (val == null || val.length() < 7 || (val.length() >= 6 && val.substring(6).trim().isEmpty()))
            return DT_DAY_EMPTY;

        if (!GEN_VALID_DATE_IOP(binding, value))
            return DT_ERROR;

        return Integer.valueOf(val.substring(6));
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param binding
     * @param value1
     * @param value2
     * @param minMaxFlagObj
     * @return - internal use only -
     */
    public int GEN_DATECMP_IOP(Binding binding, Object value1, Object value2, Object minMaxFlagObj) {
        StringBuilder buf1 = new StringBuilder();
        StringBuilder buf2 = new StringBuilder();

        int minMaxFlag = (Integer)minMaxFlagObj;

        if (minMaxFlag == DT_MIN && !runMinFlagLogic(binding, value1, value2, minMaxFlag, value1, value2))
            return 0;
        int result = applyMinMaxFlag(binding, value1, value2, minMaxFlag, buf1, buf2);
        if (result != 0)
            return result;

        LocalDate date1 = LocalDate.parse(buf1.toString(), _GEN_DATECMP_IOP_FORMAT);
        LocalDate date2 = LocalDate.parse(buf2.toString(), _GEN_DATECMP_IOP_FORMAT);

        if (date1.compareTo(date2) > 0)
            return 1;
        else if (date1.compareTo(date2) < 0)
            return -1;
        else
            return 0;
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param binding
     * @param value1
     * @param value2
     * @param minMaxFlagObj
     * @return - internal use only -
     */
    public int GEN_YEARDIFF_IOP(Binding binding, String value1, String value2, Object minMaxFlagObj) {
        int dayDiff = GEN_DAYDIFF_IOP(binding, value1, value2, minMaxFlagObj);
        if (dayDiff > SENTINEL_TRESHOLD)
            return dayDiff;

        return (int)Math.floor((double)dayDiff / (double)365);
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param binding
     * @param value1
     * @param value2
     * @param minInt
     * @param maxInt
     * @param minMaxFlagObj
     * @return - internal use only -
     */
    public int GEN_YEARINTERNAL_IOP(Binding binding, String value1, String value2, Integer minInt, Integer maxInt, Object minMaxFlagObj) {
        int yearDiff = GEN_YEARDIFF_IOP(binding, value1, value2, minMaxFlagObj);
        if (yearDiff > SENTINEL_TRESHOLD)
            return yearDiff;

        return yearDiff >= minInt && yearDiff <= maxInt ? 1 : 0;
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param binding
     * @param value1
     * @param value2
     * @param minMaxFlagObj
     * @return - internal use only -
     */
    public int GEN_MONTHDIFF_IOP(Binding binding, String value1, String value2, Object minMaxFlagObj) {
        int dayDiff = GEN_DAYDIFF_IOP(binding, value1, value2, minMaxFlagObj);
        if (dayDiff > SENTINEL_TRESHOLD)
            return dayDiff;

        return dayDiff < 0 ? (int)Math.ceil((double)dayDiff / (double)30) : (int)Math.floor((double)dayDiff / (double)30);
    }

    /**
     * Created on Apr 5, 2011 by depryf
     * @param binding
     * @param value1
     * @param value2
     * @param minInt
     * @param maxInt
     * @param minMaxFlagObj
     * @return - internal use only -
     */
    public int GEN_MONTHINTERNAL_IOP(Binding binding, String value1, String value2, Integer minInt, Integer maxInt, Object minMaxFlagObj) {
        int monthDiff = GEN_MONTHDIFF_IOP(binding, value1, value2, minMaxFlagObj);
        if (monthDiff > SENTINEL_TRESHOLD)
            return monthDiff;

        return monthDiff >= minInt && monthDiff <= maxInt ? 1 : 0;
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param binding
     * @param value1
     * @param value2
     * @param minMaxFlagObj
     * @return - internal use only -
     */
    public int GEN_DAYDIFF_IOP(Binding binding, String value1, String value2, Object minMaxFlagObj) {
        StringBuilder buf1 = new StringBuilder();
        StringBuilder buf2 = new StringBuilder();

        int minMaxFlag = (Integer)minMaxFlagObj;

        int result = applyMinMaxDayDiffFlag(binding, value1, value2, minMaxFlag, buf1, buf2);
        if (result != 0)
            return result;

        LocalDate date1 = LocalDate.parse(buf1.toString(), _GEN_DATECMP_IOP_FORMAT);
        LocalDate date2 = LocalDate.parse(buf2.toString(), _GEN_DATECMP_IOP_FORMAT);

        return (int)ChronoUnit.DAYS.between(date1, date2);
    }

    /**
     * Created on Apr 5, 2011 by depryf
     * @param binding
     * @param value1
     * @param value2
     * @param minInt
     * @param maxInt
     * @param minMaxFlagObj
     * @return - internal use only -
     */
    public int GEN_DAYINTERNAL_IOP(Binding binding, String value1, String value2, Integer minInt, Integer maxInt, Object minMaxFlagObj) {
        int dayDiff = GEN_DAYDIFF_IOP(binding, value1, value2, minMaxFlagObj);
        if (dayDiff > SENTINEL_TRESHOLD)
            return dayDiff;

        return dayDiff >= minInt && dayDiff <= maxInt ? 1 : 0;
    }

    // helper
    private boolean runMinFlagLogic(Binding binding, Object value1, Object value2, Object minMaxFlagObj, Object date1, Object date2) {
        String val1 = GEN_TO_STRING(value1);
        String val2 = GEN_TO_STRING(value2);

        if (val1 == null || val1.trim().isEmpty() || val2 == null || val2.trim().isEmpty())
            return true;

        if (!GEN_VALID_DATE_IOP(binding, val1) || !GEN_VALID_DATE_IOP(binding, val2))
            return true;

        int y1 = GEN_DATE_YEAR_IOP(binding, val1);
        int y2 = GEN_DATE_YEAR_IOP(binding, val2);
        int m1 = GEN_DATE_MONTH_IOP(binding, val1);
        int m2 = GEN_DATE_MONTH_IOP(binding, val2);
        int d1 = GEN_DATE_DAY_IOP(binding, val1);
        int d2 = GEN_DATE_DAY_IOP(binding, val2);

        if (y1 == y2 && (m1 == DT_MONTH_EMPTY || m2 == DT_MONTH_EMPTY))
            return false;

        if (y1 == y2 && m1 == m2 && (d1 == DT_DAY_EMPTY || d2 == DT_DAY_EMPTY))
            return false;

        return true;
    }

    // helper
    private int applyMinMaxDayDiffFlag(Binding binding, Object value1, Object value2, Object minMaxFlagObj, StringBuilder date1Buf, StringBuilder date2Buf) {
        String val1 = GEN_TO_STRING(value1);
        String val2 = GEN_TO_STRING(value2);

        if (val1 == null || val1.trim().isEmpty() || val2 == null || val2.trim().isEmpty())
            return DT_EMPTY;

        if (!GEN_VALID_DATE_IOP(binding, val1) || !GEN_VALID_DATE_IOP(binding, val2))
            return DT_ERROR;

        int y1 = GEN_DATE_YEAR_IOP(binding, val1);
        int y2 = GEN_DATE_YEAR_IOP(binding, val2);
        int m1 = GEN_DATE_MONTH_IOP(binding, val1);
        int m2 = GEN_DATE_MONTH_IOP(binding, val2);
        int d1 = GEN_DATE_DAY_IOP(binding, val1);
        int d2 = GEN_DATE_DAY_IOP(binding, val2);

        date1Buf.append(pad(String.valueOf(y1), 4, "0", true));
        date2Buf.append(pad(String.valueOf(y2), 4, "0", true));

        int minMaxFlag = (Integer)minMaxFlagObj;

        // handle month of first value
        int safeBeginningMonth = m1;
        if (m1 == DT_MONTH_EMPTY) {
            if (minMaxFlag == DT_MIN) {
                if (y1 == y2 && m2 != DT_MONTH_EMPTY)
                    safeBeginningMonth = m2;
                else if (y1 < y2)
                    safeBeginningMonth = 12; // December
                else
                    safeBeginningMonth = 1; // January
            }
            else if (minMaxFlag == DT_MAX)
                safeBeginningMonth = 1; // January
            else
                return DT_UNKNOWN;
        }
        date1Buf.append(pad(String.valueOf(safeBeginningMonth), 2, "0", true));

        // handle month of second value
        int safeEndMonth = m2;
        if (m2 == DT_MONTH_EMPTY) {
            if (minMaxFlag == DT_MIN) {
                if (y1 == y2)
                    safeEndMonth = safeBeginningMonth;
                else if (m1 == DT_MONTH_EMPTY && m2 == DT_MONTH_EMPTY && y1 > y2)
                    safeEndMonth = 12;
                else if (y1 > y2)
                    safeEndMonth = 12;
                else
                    safeEndMonth = 1; // January
            }
            else if (minMaxFlag == DT_MAX)
                safeEndMonth = 12; // December
            else
                return DT_UNKNOWN;
        }
        date2Buf.append(pad(String.valueOf(safeEndMonth), 2, "0", true));

        int numDaysInBeginningMonth = YearMonth.of(y1, safeBeginningMonth).lengthOfMonth();
        int numDaysInEndMonth = YearMonth.of(y2, safeEndMonth).lengthOfMonth();

        // handle day of first value
        int safeBeginningDay = d1;
        if (d1 == DT_DAY_EMPTY) {
            if (minMaxFlag == DT_MIN) {
                if (y1 == y2 && m1 != DT_MONTH_EMPTY && m2 != DT_MONTH_EMPTY && d2 != DT_DAY_EMPTY)
                    safeBeginningDay = 1;
                else if (y1 == y2 && m1 == DT_MONTH_EMPTY && d2 != DT_DAY_EMPTY)
                    safeBeginningDay = d2;
                else if (y1 < y2)
                    safeBeginningDay = numDaysInBeginningMonth; // last day
                else if (y1 > y2 || m1 == DT_MONTH_EMPTY)
                    safeBeginningDay = 1; // first day
                else
                    safeBeginningDay = numDaysInBeginningMonth; // last day
            }
            else if (minMaxFlag == DT_MAX)
                safeBeginningDay = 1; // first day
            else
                return DT_UNKNOWN;
        }
        date1Buf.append(pad(String.valueOf(safeBeginningDay), 2, "0", true));

        // handle day of second value
        int safeEndDay = d2;
        if (d2 == DT_DAY_EMPTY) {
            if (minMaxFlag == DT_MIN) {
                if (y1 == y2 && safeBeginningMonth == safeEndMonth)
                    safeEndDay = safeBeginningDay;
                else if (m1 == DT_MONTH_EMPTY && m2 == DT_MONTH_EMPTY && y1 > y2)
                    safeEndDay = numDaysInEndMonth;
                else if (y1 > y2)
                    safeEndDay = numDaysInEndMonth;
                else
                    safeEndDay = 1; // first day
            }
            else if (minMaxFlag == DT_MAX)
                safeEndDay = numDaysInEndMonth; // last day
            else
                return DT_UNKNOWN;
        }
        date2Buf.append(pad(String.valueOf(safeEndDay), 2, "0", true));

        return 0;
    }

    // helper
    private int applyMinMaxFlag(Binding binding, Object value1, Object value2, Object minMaxFlagObj, StringBuilder date1Buf, StringBuilder date2Buf) {
        String val1 = GEN_TO_STRING(value1);
        String val2 = GEN_TO_STRING(value2);

        if (val1 == null || val1.trim().isEmpty() || val2 == null || val2.trim().isEmpty())
            return DT_EMPTY;

        if (!GEN_VALID_DATE_IOP(binding, val1) || !GEN_VALID_DATE_IOP(binding, val2))
            return DT_ERROR;

        int y1 = GEN_DATE_YEAR_IOP(binding, val1);
        int y2 = GEN_DATE_YEAR_IOP(binding, val2);
        int m1 = GEN_DATE_MONTH_IOP(binding, val1);
        int m2 = GEN_DATE_MONTH_IOP(binding, val2);
        int d1 = GEN_DATE_DAY_IOP(binding, val1);
        int d2 = GEN_DATE_DAY_IOP(binding, val2);

        date1Buf.append(pad(String.valueOf(y1), 4, "0", true));
        date2Buf.append(pad(String.valueOf(y2), 4, "0", true));

        int minMaxFlag = (Integer)minMaxFlagObj;

        // handle month of first value
        int safeBeginningMonth = m1;
        if (m1 == DT_MONTH_EMPTY) {
            if (minMaxFlag == DT_MIN)
                safeBeginningMonth = 12; // December
            else if (minMaxFlag == DT_MAX)
                safeBeginningMonth = 1; // January
            else
                return DT_UNKNOWN;
        }
        date1Buf.append(pad(String.valueOf(safeBeginningMonth), 2, "0", true));

        // handle month of second value
        int safeEndMonth = m2;
        if (m2 == DT_MONTH_EMPTY) {
            if (minMaxFlag == DT_MIN) {
                if (y1 == y2)
                    safeEndMonth = safeBeginningMonth;
                else
                    safeEndMonth = 1; // January
            }
            else if (minMaxFlag == DT_MAX)
                safeEndMonth = 12; // December
            else
                return DT_UNKNOWN;
        }
        date2Buf.append(pad(String.valueOf(safeEndMonth), 2, "0", true));

        int numDaysInBeginningMonth = YearMonth.of(y1, safeBeginningMonth).lengthOfMonth();
        int numDaysInEndMonth = YearMonth.of(y2, safeEndMonth).lengthOfMonth();

        // handle day of first value
        int safeBeginningDay = d1;
        if (d1 == DT_DAY_EMPTY) {
            if (minMaxFlag == DT_MIN)
                safeBeginningDay = numDaysInBeginningMonth; // last day
            else if (minMaxFlag == DT_MAX)
                safeBeginningDay = 1; // first day
            else
                return DT_UNKNOWN;
        }
        date1Buf.append(pad(String.valueOf(safeBeginningDay), 2, "0", true));

        // handle day of second value
        int safeEndDay = d2;
        if (d2 == DT_DAY_EMPTY) {
            if (minMaxFlag == DT_MIN) {
                if (y1 == y2 && safeBeginningMonth == safeEndMonth)
                    safeEndDay = safeBeginningDay;
                else
                    safeEndDay = 1; // first day
            }
            else if (minMaxFlag == DT_MAX)
                safeEndDay = numDaysInEndMonth; // last day
            else
                return DT_UNKNOWN;
        }
        date2Buf.append(pad(String.valueOf(safeEndDay), 2, "0", true));

        return 0;
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param value
     * @param typeObj
     * @return - internal use only -
     */
    public String GEN_TRIM(Object value, Object typeObj) {
        String val = GEN_TO_STRING(value);

        if (val == null || val.isEmpty())
            return val;

        int type = (Integer)typeObj;

        String result;
        switch (type) {
            case TRIM_LEFT:
                result = _GEN_TRIM_P1.matcher(val).replaceAll("");
                break;
            case TRIM_RIGHT:
                result = _GEN_TRIM_P2.matcher(val).replaceAll("");
                break;
            case TRIM_BOTH:
                result = _GEN_TRIM_P3.matcher(val).replaceAll("");
                break;
            default:
                throw new RuntimeException("Unsupported type: " + type);
        }

        return result;
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param value
     * @return - internal use only -
     */
    public int GEN_STRLEN(Object value) {
        String val = GEN_TO_STRING(value);
        return val == null ? 0 : val.length();
    }

    // helper
    private boolean isValInList(String val, String list) {
        // if the value has trailing non-numeric values, trim off those non-numeric values first
        String numericVal = removeTrailingNonNumerics(val);

        for (String term : StringUtils.split(list, ',')) {
            String[] parts = StringUtils.split(term, '-');

            // term is single value
            if (parts.length == 1 && val.equals(term.trim()))
                return true;
                // term is a range
            else if (parts.length == 2) {
                // if the input value is a digit
                if (numericVal != null) {
                    String low = parts[0].trim(), high = parts[1].trim();

                    //remove trailing non-numeric values from range numbers
                    low = removeTrailingNonNumerics(low);
                    high = removeTrailingNonNumerics(high);

                    // if both the low and high values are numeric
                    if (low != null && high != null) {
                        int iVal = Integer.parseInt(numericVal);
                        int iLow = Integer.parseInt(low);
                        int iHigh = Integer.parseInt(high);
                        if (iVal >= iLow && iVal <= iHigh)
                            return true;
                    }// if low is numeric but the high value is non-numeric
                    else if (low != null && high == null) {
                        int iVal = Integer.parseInt(numericVal);
                        int iLow = Integer.parseInt(low);
                        if (iVal >= iLow)
                            return true;
                    }// if low is non-numeric but the high value is numeric
                    else if (low == null && high != null) {
                        int iVal = Integer.parseInt(numericVal);
                        int iHigh = Integer.parseInt(high);
                        if (iVal <= iHigh)
                            return true;
                    }
                    else
                        return true;
                }
            }
        }

        return false;
    }

    // helper
    private String removeTrailingNonNumerics(String input) {
        String newString = null;
        Matcher m4 = _GEN_INLIST_P1.matcher(input);
        if (m4.matches())
            newString = m4.group(1);

        return newString;
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param value
     * @param list
     * @return - internal use only -
     */
    public boolean GEN_INLIST(Object value, Object list) {
        String val = GEN_TO_STRING(value);
        String l = GEN_TO_STRING(list);

        // special case for the list
        if (l == null || l.isEmpty())
            return false;

        // apparently Genedits trim the incoming value (see SEER*Utils Squish issue #49)
        if (val != null)
            val = val.trim();

        // this make the assumption that the empty value in the list will always be at the end...
        if (val == null || val.isEmpty())
            return l.trim().isEmpty() || l.trim().endsWith(",");

        return isValInList(val, l);
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param value
     * @param list
     * @param regex
     * @return - internal use only -
     */
    public boolean GEN_INLIST(Object value, Object list, Object regex) {
        if (value == null)
            return false;

        String l = GEN_TO_STRING(list);
        if (l.isEmpty())
            return false;

        if (!GEN_MATCH(value, regex))
            return false;

        // apparently Genedits right-trim the incoming value in their MATCH method, in C++ the value is a pointer, so it is trimmed in INLIST as a side effect!
        String val = trimRight(GEN_TO_STRING(value));
        if (val.isEmpty())
            return true;

        return isValInList(val, l);
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param value
     * @param list
     * @param regex
     * @param startPos
     * @param length
     * @return - internal use only -
     */
    public boolean GEN_INLIST(Object value, Object list, Object regex, Integer startPos, Integer length) {
        if (value == null)
            return false;

        String l = GEN_TO_STRING(list);
        if (l.isEmpty())
            return false;

        if (!GEN_MATCH(value, regex))
            return false;

        // apparently Genedits right-trim the incoming value in their MATCH method, in C++ the value is a pointer, so it is trimmed in INLIST as a side effect!
        String val = trimRight(GEN_TO_STRING(value));

        if (val.isEmpty())
            return true;

        int start = startPos - 1; // Genedits uses 1-based index...
        int end = Math.min(startPos - 1 + length, val.length()); // Gendits allows a length going past the end of the string...

        if (val.trim().isEmpty() || start >= end)
            return false;

        return isValInList(val.substring(start, end), l);
    }

    public boolean GEN_MATCH(Object value, Object regex) {
        if (value == null)
            return false;

        String val = GEN_TO_STRING(value);
        String reg = GEN_TO_STRING(regex);

        // apparently Genedits right-trim the incoming value
        val = trimRight(val);

        // there isn't supposed to be any spaces in the regex but it looks like it does happen...
        reg = _GEN_MATCH_P1.matcher(reg).replaceAll("\\\\s");

        // looks like Genedits considers a regex for a single space to match an empty string...
        if (val.isEmpty()) {
            String tmp = _GEN_MATCH_P2.matcher(reg).replaceAll("\\\\s"); // let's deal only with single spaces, since they seem to do the same in the Genedits language
            if (tmp.equals("\\s") || tmp.startsWith("\\s|") || tmp.endsWith("|\\s") || tmp.equals("(\\s)") || tmp.startsWith("(\\s|") || tmp.endsWith("|\\s)"))
                return true;
        }

        // deal with the right spaces
        reg = _GEN_MATCH_P3.matcher(reg).replaceAll(")");

        // and again, right spaces stuff...
        reg = _GEN_MATCH_P4.matcher(reg).replaceAll("");

        return val.matches(reg);
    }

    public boolean GEN_LOOKUP(Object value, Object tableObj, Object indexObj, Map<Integer, char[]> tableVars) {
        // TODO
    }

    // this is the old engine (v4) implementation
    @SuppressWarnings("unchecked")
    private boolean GEN_LOOKUP_V4(Object value, Object tableObj, Object indexObj, Map<Integer, char[]> tableVars) {
        String val = GEN_TO_STRING(value);

        if (val == null || (tableObj == null && indexObj == null))
            return false;

        // index values are trimmed, so the incoming one also need to be trimmed...
        String trimmedVal = trimRight(val);

        List<List<Object>> table = (List<List<Object>>)tableObj;

        // if an index exists, use it!
        boolean found = false;
        Integer valIndex = null;
        if (indexObj != null) {
            if (indexObj instanceof List) {
                for (Object elem : (List<Object>)indexObj) {
                    if (elem instanceof List) {
                        List<Object> indexAndRecNum = (List<Object>)elem;
                        if (indexAndRecNum.size() != 2)
                            throw new RuntimeException("List elements of indexes must be of size 2 (index value and row number)");
                        Object indexStr = indexAndRecNum.get(0);
                        if (!(indexStr instanceof String))
                            throw new RuntimeException("Index values must be Strings; got " + indexStr.getClass().getSimpleName());
                        int comp = trimmedVal.compareTo((String)indexStr);
                        if (comp == 0) {
                            found = true;
                            valIndex = (Integer)indexAndRecNum.get(1);
                            break;
                        }
                        else if (comp < 0)
                            break; // values in the index are sorted, so we can stop the iteration sooner...

                    }
                    else if (elem instanceof String) {
                        int comp = trimmedVal.compareTo((String)elem);
                        if (comp == 0) {
                            found = true;
                            break;
                        }
                        else if (comp < 0)
                            break; // values in the index are sorted, so we can stop the iteration sooner...
                    }
                    else
                        throw new RuntimeException("Index elements must be Strings or Lists; got " + elem.getClass().getSimpleName());
                }
            }
            else if (indexObj instanceof Map) {
                Object rowNumObject = ((Map<Object, Object>)indexObj).get(trimmedVal);
                if (rowNumObject != null) {
                    found = true;
                    if (rowNumObject instanceof Integer)
                        valIndex = (Integer)rowNumObject;
                    else
                        throw new RuntimeException("Row numbers in indexes have to be Integers, got " + rowNumObject.getClass().getSimpleName());
                }
            }
            else if (indexObj instanceof Set)
                found = ((Set<Object>)indexObj).contains(trimmedVal);
            else
                throw new RuntimeException("Unsupported index type: " + indexObj.getClass().getSimpleName());
        }
        else { // otherwise go over the entire table (ignore header)
            for (int i = 1; i < table.size(); i++) {
                StringBuilder buf = new StringBuilder();
                for (Object cell : table.get(i))
                    buf.append(cell);
                if (trimmedVal.equals(trimRight(buf.toString()))) {
                    found = true;
                    valIndex = Integer.valueOf(i);
                    break;
                }
            }
        }

        // side effect, fill in any requested tableVar
        if (tableVars != null && table != null) {
            List<Object> row = found && valIndex != null ? table.get(valIndex) : null;
            for (Map.Entry<Integer, char[]> entry : tableVars.entrySet()) {
                if (row == null)
                    GEN_STRCPY(entry.getValue(), "");
                else {
                    int colIdx = entry.getKey();
                    if (colIdx >= 0 && colIdx < row.size()) {
                        Object obj = row.get(colIdx);
                        GEN_STRCPY(entry.getValue(), trimRight(obj == null ? "" : obj.toString()));
                    }
                }
            }
        }

        return found;
    }

    // in the new engine (v5), they kept LOOKUP but it looks like it will be deprecated at some point...
    private boolean GEN_LOOKUP_V5(Object value, ContextTable table, ContextTableIndex index, Map<Integer, char[]> tableVars) {
        // TODO
    }

    /**
     * Special genedit method. Internal use only.
     */
    @SuppressWarnings("unchecked")
    public boolean GEN_RLOOKUP(Object value, Object tableObj, Object indexObj, Map<Integer, char[]> tableVars) {
        String val = GEN_TO_STRING(value);

        // it makes no sense to use RLOOKUP without an index!
        if (val == null || indexObj == null)
            return false;

        List<List<String>> table = (List<List<String>>)tableObj;

        Integer valIndex = null;

        if (indexObj instanceof List) {
            List<List<Object>> indexList = (List<List<Object>>)indexObj;

            // for RLOOKUP, if a value is smaller than the smallest index value, return value not found
            List<Object> firstIndexAndRecNum = indexList.get(0);
            if (val.compareTo((String)firstIndexAndRecNum.get(0)) >= 0) {

                // find the highest record for the requested value
                for (int indexIdx = 0; indexIdx < indexList.size(); indexIdx++) {
                    List<Object> indexAndRecNum = indexList.get(indexIdx);
                    String indexVal = (String)indexAndRecNum.get(0);
                    int comp = val.compareTo(indexVal);
                    if (comp == 0) {
                        valIndex = (Integer)indexAndRecNum.get(1);
                        break;
                    }
                    else if (comp < 0) {
                        valIndex = (Integer)indexList.get(indexIdx - 1).get(1);
                        break;
                    }
                }

                // for RLOOKUP, if a value is higher than the highest index value, use the last index value
                if (valIndex == null) {
                    List<Object> lastIndexAndRecNum = indexList.get(indexList.size() - 1);
                    String indexVal = (String)lastIndexAndRecNum.get(0);
                    if (val.compareTo(indexVal) > 0)
                        valIndex = (Integer)lastIndexAndRecNum.get(1);
                }
            }
        }
        else if (indexObj instanceof TreeMap) {
            TreeMap<String, Integer> indexTree = (TreeMap<String, Integer>)indexObj;

            // for RLOOKUP, if a value is smaller than the smallest index value, return value not found
            if (val.compareTo(indexTree.firstKey()) >= 0) {

                // find the highest record for the requested value
                Map.Entry<String, Integer> entry = indexTree.floorEntry(val);
                if (entry != null)
                    valIndex = entry.getValue();
                    // for RLOOKUP, if a value is higher than the highest index value, use the last index value
                else if (val.compareTo(indexTree.lastKey()) > 0)
                    valIndex = indexTree.lastEntry().getValue();

            }
        }
        else
            throw new RuntimeException("Unsupported index type: " + indexObj.getClass().getSimpleName());

        boolean found = valIndex != null && valIndex.intValue() >= 0;

        // side effect, fill in any requested tableVar
        if (tableVars != null && table != null) {
            List<String> row = found && valIndex != null ? table.get(valIndex) : null;
            for (Map.Entry<Integer, char[]> entry : tableVars.entrySet()) {
                if (row == null)
                    GEN_STRCPY(entry.getValue(), "");
                else {
                    int colIdx = entry.getKey();
                    if (colIdx >= 0 && colIdx < row.size())
                        GEN_STRCPY(entry.getValue(), GEN_TRIM(row.get(colIdx), TRIM_RIGHT));
                }
            }
        }

        return found;
    }

    /**
     * Special genedit method. Internal use only.
     */
    @SuppressWarnings("unchecked")
    public boolean GEN_ILOOKUP(Object value, Object indexObj) {
        String val = GEN_TO_STRING(value);

        if (val == null || indexObj == null)
            return false;

        // index values are trimmed, so the incoming one also need to be trimmed...
        String trimmedVal = trimRight(val);

        boolean found = false;
        if (indexObj instanceof List) {
            for (Object elem : (List<Object>)indexObj) {
                if (elem instanceof List) {
                    List<Object> indexAndRecNum = (List<Object>)elem;
                    if (indexAndRecNum.size() != 2)
                        throw new RuntimeException("List elements of indexes must be of size 2 (index value and row number)");
                    Object indexStr = indexAndRecNum.get(0);
                    if (!(indexStr instanceof String))
                        throw new RuntimeException("Index values must be Strings; got " + indexStr.getClass().getSimpleName());
                    int comp = trimmedVal.compareTo((String)indexStr);
                    if (comp == 0) {
                        found = true;
                        break;
                    }
                    else if (comp < 0)
                        break; // values in the index are sorted, so we can stop the iteration sooner...

                }
                else if (elem instanceof String) {
                    int comp = trimmedVal.compareTo((String)elem);
                    if (comp == 0) {
                        found = true;
                        break;
                    }
                    else if (comp < 0)
                        break; // values in the index are sorted, so we can stop the iteration sooner...
                }
                else
                    throw new RuntimeException("Index elements must be Strings or Lists; got " + elem.getClass().getSimpleName());
            }
        }
        else if (indexObj instanceof Map)
            found = ((Map<Object, Object>)indexObj).containsKey(trimmedVal);
        else if (indexObj instanceof Set)
            found = ((Set<Object>)indexObj).contains(trimmedVal);
        else
            throw new RuntimeException("Unsupported index type: " + indexObj.getClass().getSimpleName());

        return found;
    }

    /**
     * Special genedit method. Internal use only.
     */
    public Integer GEN_BINLOOKUP(List<List<Integer>> table, Object indexObj) {
        if (table == null || table.isEmpty())
            return null;

        // Genedits is 1-based
        int index = (Integer)indexObj - 1;

        int rowSize = table.get(0).size();
        int row = index / rowSize;
        int col = index % rowSize;

        // return 0 if any of the indexes is out of range
        if (row < 0 || row >= table.size())
            return 0;

        List<Integer> rowData = table.get(row);
        if (col < 0 || col >= rowData.size())
            return 0;

        return rowData.get(col);
    }

    /**
     * Special genedit method. Internal use only.
     */
    public boolean GEN_SQLLOOKUP(List<List<Object>> table, List<Object> columnNames, Object value) {
        if (columnNames.isEmpty())
            return true;

        String val = GEN_TO_STRING(value);

        int[] colNumbers = new int[columnNames.size()];
        for (int i = 0; i < colNumbers.length; i++) {
            colNumbers[i] = table.get(0).indexOf(columnNames.get(i));
            if (colNumbers[i] == -1)
                return true;
        }

        for (int r = 1; r < table.size(); r++) {
            List<Object> row = table.get(r);
            StringBuilder rowVal = new StringBuilder("");
            for (int c : colNumbers)
                rowVal.append(GEN_TO_STRING(row.get(c)));

            if (val.equals(rowVal.toString()))
                return true;
        }
        return false;
    }

    /**
     * Special genedit method. Internal use only.
     */
    public Integer GEN_BINLOOKUP(List<List<Integer>> table, Object rowObj, Object colObj) {

        // adjust the indexes (GENEDITS is 1-based, we are 0-based)
        int row = (Integer)rowObj - 1;
        int col = (Integer)colObj - 1;

        // return 0 if any of the indexes is out of range
        if (row < 0 || row >= table.size())
            return 0;

        List<Integer> rowData = table.get(row);
        if (col < 0 || col >= rowData.size())
            return 0;

        return rowData.get(col);
    }

    /**
     * Special genedit method. Internal use only.
     */
    public boolean GEN_SQLRANGELOOKUP(List<List<Object>> table, List<Object> columnNames, Object value, Map<Integer, char[]> tableVars) {
        if (columnNames.isEmpty())
            return true;

        String val = GEN_TO_STRING(value);

        int[] colNumbers = new int[columnNames.size()];
        for (int i = 0; i < colNumbers.length; i++) {
            colNumbers[i] = table.get(0).indexOf(columnNames.get(i));
            if (colNumbers[i] == -1)
                return false;
        }

        int indexOfLargest = -1;
        String largest = "";
        for (int r = 1; r < table.size(); r++) {
            List<Object> row = table.get(r);
            StringBuilder rowVal = new StringBuilder("");
            for (int c : colNumbers)
                rowVal.append(GEN_TO_STRING(row.get(c)));
            if (StringUtils.isNumeric(val) && StringUtils.isNumeric(rowVal)) {
                Double rowNum = Double.parseDouble(rowVal.toString());
                if (rowNum <= Double.parseDouble(val) && (StringUtils.isEmpty(largest) || rowNum > Double.parseDouble(largest))) {
                    indexOfLargest = r;
                    largest = rowVal.toString();
                }
            }
            else if (rowVal.toString().compareTo(val) <= 0 && rowVal.toString().compareTo(largest) > 0) {
                indexOfLargest = r;
                largest = rowVal.toString();
            }
        }

        // side effect, fill in any requested tableVar
        if (indexOfLargest > -1 && tableVars != null) {
            List<Object> row = table.get(indexOfLargest);
            for (Map.Entry<Integer, char[]> entry : tableVars.entrySet()) {
                if (row == null)
                    GEN_STRCPY(entry.getValue(), "");
                else {
                    int colIdx = entry.getKey();
                    if (colIdx >= 0 && colIdx < row.size()) {
                        Object obj = row.get(colIdx);
                        GEN_STRCPY(entry.getValue(), trimRight(obj == null ? "" : obj.toString()));
                    }
                }
            }
        }
        return indexOfLargest > -1;
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param value
     * @param start
     * @return - internal use only -
     */
    public char[] GEN_SUBSTR(Object value, Integer start) {
        if (value == null)
            return new char[0];

        String str = GEN_TO_STRING(value);
        if (str.length() >= start) {
            str = trimRight(str.substring(start - 1));
        }

        return str.toCharArray();
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param value
     * @param start
     * @param length
     * @return - internal use only -
     */
    public char[] GEN_SUBSTR(Object value, Integer start, Integer length) {
        if (value == null)
            return new char[0];

        String str = GEN_TO_STRING(value);
        if (str.length() >= start) {
            str = str.substring(start - 1);
            if (str.length() > length)
                str = str.substring(0, length);
        }

        return str.toCharArray();
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param target
     * @param value
     */
    public void GEN_STRCPY(char[] target, Object value) {
        GEN_STRCPY(target, value, value == null ? 0 : GEN_TO_STRING(value).length());
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param target
     * @param value
     * @param num
     */
    public void GEN_STRCPY(char[] target, Object value, Integer num) {
        if (value == null)
            return;

        String val = GEN_TO_STRING(value);

        boolean pad = false;
        if (num < 0) {
            num = num * -1;
            pad = true;
        }
        for (int i = 0; i < num && i < val.length() && i < target.length - 1; i++)
            target[i] = val.charAt(i);
        if (pad)
            for (int i = val.length(); i < num && i < target.length - 1; i++)
                target[i] = ' ';
        target[num] = '\0'; // go C++!
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param target
     * @param value
     * @return - internal use only -
     */
    public char[] GEN_STRCAT(char[] target, Object value) {
        return GEN_STRCAT(target, value, value == null ? 0 : GEN_TO_STRING(value).length());
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param target
     * @param value
     * @param num
     * @return - internal use only -
     */
    public char[] GEN_STRCAT(char[] target, Object value, Integer num) {
        if (value == null || num <= 0)
            return target;

        String val = GEN_TO_STRING(value);

        int idx = -1;
        for (idx = 0; idx < target.length; idx++)
            if (target[idx] == '\0')
                break;

        for (int i = 0; i < num && i < val.length() && idx < target.length - 1; i++)
            target[idx++] = val.charAt(i);
        target[idx] = '\0'; // go C++!

        return target;
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param value1
     * @param value2
     * @return - internal use only -
     */
    public int GEN_STRCMP(Object value1, Object value2) {
        return GEN_STRCMP(value1, value2, null);
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param value1
     * @param value2
     * @param length
     * @return - internal use only -
     */
    public int GEN_STRCMP(Object value1, Object value2, Integer length) {
        if (value1 == null || value2 == null)
            return -1;

        String s1 = GEN_TO_STRING(value1);
        String s2 = GEN_TO_STRING(value2);
        if (length != null) {
            s1 = GEN_TO_STRING(GEN_SUBSTR(value1, 1, length));
            s2 = GEN_TO_STRING(GEN_SUBSTR(value2, 1, length));
        }

        int comp = s1.compareTo(s2);
        if (comp < 0)
            return -1;
        else if (comp > 0)
            return 1;
        else
            return 0;
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param target
     * @param format
     * @param value
     */
    public void GEN_FMTSTR(char[] target, Object format, Object value) {
        if (target == null || format == null || value == null)
            return;

        GEN_STRCPY(target, String.format(_GEN_FMTSTR_P1.matcher(GEN_TO_STRING(format)).replaceAll("%$1d"), value));
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param value
     * @param text
     * @return - internal use only -
     */
    public int GEN_AT(Object value, Object text) {
        return GEN_AT(value, text, 1);
    }

    /**
     * Created on Apr 5, 2011 by depryf
     * @param value
     * @param text
     * @param width
     * @return - internal use only -
     */
    public int GEN_AT(Object value, Object text, Integer width) {
        if (value == null || text == null)
            return 0;

        String val = GEN_TO_STRING(value);
        String txt = GEN_TO_STRING(text);

        int idx = txt.indexOf(val);
        while (idx != -1 && idx % width != 0)
            idx = txt.indexOf(val, idx + 1);

        return idx + 1;
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param value
     * @param justifiedObj
     * @return - internal use only -
     */
    public boolean GEN_JUSTIFIED(Object value, Object justifiedObj) {
        String val = GEN_TO_STRING(value);

        int justified = (Integer)justifiedObj;

        if (justified == JUSTIFIED_LEFT)
            return val != null && val.length() > 0 && val.charAt(0) != ' ';
        else if (justified == JUSTIFIED_RIGHT)
            return val != null && val.length() > 0 && value.toString().charAt(val.length() - 1) != ' ';
        else
            throw new RuntimeException("Bad parameter to GEN_JUSTIFIED: " + justified);
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param value
     * @param numChars
     * @return - internal use only -
     */
    public char[] GEN_RIGHT(Object value, Integer numChars) {
        String val = GEN_TO_STRING(value);

        if (val == null)
            return new char[0];

        if (val.length() <= numChars)
            return val.toCharArray();

        return val.substring(val.length() - numChars).toCharArray();
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param value
     * @param numChars
     * @return - internal use only -
     */
    public char[] GEN_LEFT(Object value, Integer numChars) {
        String val = GEN_TO_STRING(value);

        if (val == null)
            return new char[0];

        if (val.length() <= numChars)
            return val.toCharArray();

        return val.substring(0, numChars).toCharArray();
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param value
     * @return - internal use only -
     */
    public char[] GEN_LOWER(Object value) {
        String val = GEN_TO_STRING(value);

        if (val == null)
            return new char[0];

        return val.toLowerCase().toCharArray();
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param value
     * @return - internal use only -
     */
    public char[] GEN_UPPER(Object value) {
        String val = GEN_TO_STRING(value);

        if (val == null)
            return new char[0];

        return val.toUpperCase().toCharArray();
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param binding
     * @param obj
     * @return - internal use only -
     */
    public boolean GEN_ERROR_TEXT(Binding binding, Object obj) {

        /**
         * ERROR_TEXT changes the error message to a literal string, allowing an edit to return a context-specific error message.
         * ERROR_TEXT always returns FAIL and can be used with a RETURN statement.
         */

        String text = GEN_TO_STRING(obj);
        if (text != null)
            text = trimRight(text);
        if (binding.hasVariable(BINDING_KEY_DATE_COMPONENT)) {
            String dateComp = (String)binding.getVariable(BINDING_KEY_DATE_COMPONENT);
            if (text != null && text.contains("%DC") && dateComp != null)
                text = text.replace("%DC", dateComp);
        }
        binding.setVariable(ValidationEngine.VALIDATOR_ERROR_MESSAGE, text);

        return false;
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param binding
     * @param obj
     * @return - internal use only -
     */
    public boolean GEN_ERROR_MSG(Binding binding, Object obj) {

        /**
         * ERROR_MSG changes the error message number of an edit, allowing an edit to return a context-specific error message.
         * ERROR_MSG always returns FAIL so it may be used with a RETURN statement to FAIL an edit.
         */

        // we don't support error numbers, they are translated to the actual error messages; so this method is the same as ERROR_TEXT...
        return GEN_ERROR_TEXT(binding, obj);
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param binding
     * @param text
     */
    public boolean GEN_SAVE_TEXT(Binding binding, Object text) {
        if (text == null)
            return false;

        /**
         * Generates messages but does not set error flags. This function causes FAIL to be returned from the edit.
         * Messages reported by SAVE_TEXT are in addition to any reported by ERROR_MSG or other returns from the edit.
         */

        @SuppressWarnings("unchecked")
        List<String> extraErrorMsgs = (List<String>)binding.getVariable(ValidationEngine.VALIDATOR_INFORMATION_MESSAGES);
        if (extraErrorMsgs == null) {
            extraErrorMsgs = new ArrayList<String>();
            binding.setVariable(ValidationEngine.VALIDATOR_INFORMATION_MESSAGES, extraErrorMsgs);
        }
        extraErrorMsgs.add(trimRight(GEN_TO_STRING(text)));

        // this method does NOT set the failure flag, this has been verified using GeneditsPlus...

        return false;
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param binding
     * @param texts
     */
    public boolean GEN_SAVE_ERROR_TEXT(Binding binding, Object... texts) {

        /**
         * Generates error messages but does not set flags. This function causes FAIL to be returned from the edit.
         * It can be used to return multiple error messages from the same edit, unlike ERROR_MSG, which changes the default error message only.
         * Errors reported by SAVE_ERROR_TEXT are in addition to any reported by ERROR_MSG or other returns from the edit.
         */

        // we don't support error numbers, they are translated to the actual error messages...

        @SuppressWarnings("unchecked")
        List<String> extraErrorMsgs = (List<String>)binding.getVariable(ValidationEngine.VALIDATOR_EXTRA_ERROR_MESSAGES);
        if (extraErrorMsgs == null) {
            extraErrorMsgs = new ArrayList<String>();
            binding.setVariable(ValidationEngine.VALIDATOR_EXTRA_ERROR_MESSAGES, extraErrorMsgs);
        }
        for (Object text : texts)
            if (text != null)
                extraErrorMsgs.add(trimRight(GEN_TO_STRING(text)));

        // this edits DOES SET the failure flag, unlike what is specified in their documentation; this has been verified with GeneditsPlus...
        binding.setVariable(ValidationEngine.VALIDATOR_FAILING_FLAG, Boolean.TRUE);

        return false;
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param binding
     * @param texts
     */
    public boolean GEN_SAVE_WARNING_TEXT(Binding binding, Object... texts) {

        /**
         * Generates warning messages but does not set flags. This function causes WARN to be returned from the edit.
         * It can be used to return multiple warning or error messages from the same edit, unlike ERROR_MSG, which changes the default error message only.
         * Warnings reported by SAVE_WARNING_TEXT are in addition to any reported by ERROR_MSG or other returns from the edit.
         */

        if (_failWarnings)
            return GEN_SAVE_WARNING_TEXT(binding, texts);

        return true;
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param binding
     * @param texts
     */
    public boolean GEN_SET_WARNING(Binding binding, Object... texts) {

        /**
         * Sets one or more flags in the flag_array and generates corresponding error messages.  This function causes WARN to be returned from the edit.
         * It can be used to return multiple warning or error messages from the same edit, unlike ERROR_MSG, which changes the default error message only.
         * Warnings reported by SET_WARNING are in addition to any reported by ERROR_MSG or other returns from the edit.
         */

        if (_failWarnings)
            return GEN_SET_ERROR(binding, texts);

        return true;
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param binding
     * @param texts
     */
    public boolean GEN_SET_ERROR(Binding binding, Object... texts) {

        // this method is not documented in Genedits; I can only guess what it does, which is setting the given error message and the failure flag...

        // we don't support error numbers, they are translated to the actual error messages...

        @SuppressWarnings("unchecked")
        List<String> extraErrorMsgs = (List<String>)binding.getVariable(ValidationEngine.VALIDATOR_EXTRA_ERROR_MESSAGES);
        if (extraErrorMsgs == null) {
            extraErrorMsgs = new ArrayList<String>();
            binding.setVariable(ValidationEngine.VALIDATOR_EXTRA_ERROR_MESSAGES, extraErrorMsgs);
        }
        for (Object text : texts)
            if (text != null)
                extraErrorMsgs.add(trimRight(GEN_TO_STRING(text)));

        binding.setVariable(ValidationEngine.VALIDATOR_FAILING_FLAG, Boolean.TRUE);

        return false;
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param params
     */
    public boolean GEN_USR2(Object... params) {
        if (params.length > 0) {
            // binding must be the first params; it wasn't always the case, so just ignore the call if it's not there...
            if (params[0] instanceof Binding) {
                Binding binding = (Binding)params[0];
                binding.setVariable(ValidationEngine.VALIDATOR_FAILING_FLAG, Boolean.TRUE);
                if (params.length > 1) {
                    String msg = GEN_TO_STRING(params[1]);
                    if (msg != null && !msg.matches("\\d+")) {
                        @SuppressWarnings("unchecked")
                        List<String> extraErrorMsgs = (List<String>)binding.getVariable(ValidationEngine.VALIDATOR_EXTRA_ERROR_MESSAGES);
                        if (extraErrorMsgs == null) {
                            extraErrorMsgs = new ArrayList<String>();
                            binding.setVariable(ValidationEngine.VALIDATOR_EXTRA_ERROR_MESSAGES, extraErrorMsgs);
                        }
                        extraErrorMsgs.add(trimRight(msg));
                    }
                }
            }
        }
        return false;
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param params
     * @return - internal use only -
     */
    public boolean GEN_USR4(Object... params) {
        return true; // this feature is currently not supported in our validation engine...
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param params
     * @return - internal use only -
     */
    public boolean GEN_GETFIELD(Object... params) {
        throw new RuntimeException("GEN_GETFIELD method is currently not supported!"); // coudn't find a single edit using this method
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param params
     * @return - internal use only -
     */
    public boolean GEN_PUTFIELD(Object... params) {
        throw new RuntimeException("GEN_PUTFIELD method is currently not supported!"); // coudn't find a single edit using this method
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param params
     * @return - internal use only -
     */
    public boolean GEN_GETVAR(Object... params) {
        throw new RuntimeException("GEN_GETVAR method is currently not supported!"); // coudn't find a single edit using this method
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param params
     * @return - internal use only -
     */
    public boolean GEN_SETVAR(Object... params) {
        throw new RuntimeException("GEN_PUTFIELD method is currently not supported!"); // coudn't find a single edit using this method
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param params
     * @return - internal use only -
     */
    public boolean GEN_NAMEEXPR(Object... params) {
        throw new RuntimeException("GEN_NAMEEXPR method is currently not supported!"); // coudn't find a single edit using this method
    }

    public int GEN_EXTERNALDLL(String dll, String method) {
        return GEN_EXTERNALDLL(dll, method, null, null, null, null, null);
    }

    public int GEN_EXTERNALDLL(String dll, String method, Object param1) {
        return GEN_EXTERNALDLL(dll, method, param1, null, null, null, null);
    }

    public int GEN_EXTERNALDLL(String dll, String method, Object param1, Object param2) {
        return GEN_EXTERNALDLL(dll, method, param1, param2, null, null, null);
    }

    public int GEN_EXTERNALDLL(String dll, String method, Object param1, Object param2, Object param3) {
        return GEN_EXTERNALDLL(dll, method, param1, param2, param3, null, null);
    }

    public int GEN_EXTERNALDLL(String dll, String method, Object param1, Object param2, Object param3, Object param4) {
        return GEN_EXTERNALDLL(dll, method, param1, param2, param3, param4, null);
    }

    public int GEN_EXTERNALDLL(String dll, String method, Object param1, Object param2, Object param3, Object param4, Object param5) {
        if (!"Cstage.dll".equalsIgnoreCase(dll) && !"Cstage0205.dll".equalsIgnoreCase(dll))
            throw new RuntimeException("Only cstage.dll and cstage0205.dll are currently supported!");

        if ("CStage_get_version".equalsIgnoreCase(method)) {
            GEN_STRCPY((char[])param1, getCsVersion());
        }
        else if ("CStage_get_number_of_schemas".equalsIgnoreCase(method))
            return getCsNumSchemas();
        else if ("CStage_get_schema_number".equalsIgnoreCase(method)) {
            String site = GEN_TO_STRING(param1);
            String hist = GEN_TO_STRING(param2);
            if (site == null || site.trim().isEmpty() || hist == null || hist.trim().isEmpty())
                return -1;
            Map<String, String> input = new HashMap<String, String>();
            input.put("primarySite", site);
            input.put("histologyIcdO3", hist);
            input.put("csSiteSpecificFactor25", GEN_TO_STRING(param3));
            return getCsSchemaNumber(input);
        }
        else if ("CStage_get_schema_name".equalsIgnoreCase(method))
            GEN_STRCPY((char[])param2, GEN_TRIM(getCsSchemaName((Integer)param1), TRIM_RIGHT));
        else if ("CStage_code_is_valid".equalsIgnoreCase(method)) {
            Integer schemaNumber = (Integer)param1;
            Integer tableNumber = (Integer)param2;
            // field index (param[2]) should always be 1 according to the DLL documentation, so it's not used
            String valueToCheck = GEN_TO_STRING(param4);
            return isAcceptableCsCode(schemaNumber, tableNumber, valueToCheck) ? 1 : 0;
        }
        else
            throw new RuntimeException("Unsupported method: " + method);

        return 0;
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on Apr 5, 2011 by depryf
     * @param obj
     * @return - internal use only -
     */
    public String GEN_TO_STRING(Object obj) {
        if (obj == null)
            return null;

        if (obj instanceof String)
            return (String)obj;
        else if (obj instanceof char[]) {
            char[] array = (char[])obj;
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < array.length; i++) {
                if (array[i] == '\0')
                    break;
                buf.append(array[i]);
            }
            return buf.toString();
        }
        else
            return obj.toString();
    }

    /**
     * Special genedit method. Internal use only.
     * <p/>
     * Created on August 14, 2013 by depryf
     * @return - internal use only -
     */
    public String GEN_DT_TODAY() {
        StringBuilder buf = new StringBuilder();

        // create a string with today's date, format is "YYYYmmDD"
        LocalDate now = LocalDate.now();
        buf.append(now.getYear());
        buf.append(pad(String.valueOf(now.getMonthValue()), 2, "0", true));
        buf.append(pad(String.valueOf(now.getDayOfMonth()), 2, "0", true));

        return buf.toString();
    }

    private static String pad(String value, int length, String with, boolean leftPad) {
        if (value == null || value.length() >= length)
            return value;

        StringBuilder builder = new StringBuilder(value);
        while (builder.length() < length)
            if (leftPad)
                builder.insert(0, with);
            else
                builder.append(with);

        return builder.toString();
    }

    private static String trimRight(String value) {
        if (value == null || value.isEmpty())
            return value;
        char[] val = value.toCharArray();
        int end = val.length;
        while ((end > 0) && (val[end - 1] <= ' '))
            end--;

        return value.substring(0, end);
    }

    public boolean GEN_WARNING_RESULT() {
        // if _failWarnings is true, then the warnings should be considered as errors and this method should return false (to fail the edit)
        // if _failWarnings is false, then the warnings should be ignored, the edits shouldn't fail and this method should return true
        return !_failWarnings;
    }
}
