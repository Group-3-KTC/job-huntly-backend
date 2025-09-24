package com.jobhuntly.backend.constant;

public final class CacheConstant {
    private CacheConstant() {}

    // ===== Job =====
    public static final String JOB_DETAIL         = "job:detail";         // key = jobId
    public static final String JOB_LIST_DEFAULT   = "job:list";           // key = sort,page,size
    public static final String JOB_LIST_SEARCH    = "job:list:search";    // key = q,city,filters,page,size
    public static final String JOB_RELATED        = "job:related";        // key = jobId

    // ===== Company =====
    public static final String COMPANY_DETAIL     = "company:detail";     // key = companyId
    public static final String COMPANY_LIST       = "company:list";       // key = sort,page,size
    public static final String COMPANY_TOP        = "company:top";        // key = period

    // ===== Dictionaries / lookups (ít đổi) =====
    public static final String DICT_CATEGORIES    = "dict:categories";
    public static final String DICT_LEVELS        = "dict:levels";
    public static final String DICT_WORK_TYPES    = "dict:worktypes";
    public static final String DICT_SKILLS        = "dict:skillsByCategoryName";
    public static final String DICT_LOCATIONS_CITY = "dict:cities";     // cities
    public static final String DICT_LOCATIONS_WARDS = "dict:wardsByCity";   // key: cityId
    public static final String DICT_PACKAGES = "dict:packagesForVip";

    // ===== User / Profile =====
    public static final String USER_BY_ID         = "user:byId";          // key = userId
    public static final String USER_BY_EMAIL      = "user:byEmail";       // key = email
    public static final String PROFILE_DETAIL     = "profile:detail";     // key = userId

    // ===== Saved jobs / Applications =====
    public static final String SAVED_JOBS         = "savedjobs:list";     // key = userId,page,size
    public static final String APPLICATIONS_LIST  = "applications:list";  // key = userId,page,size
    public static final String APPLICATION_DETAIL = "application:detail";

    // ===== AI =====
    public static final String AI_MATCH           = "ai:match";            // key = userId:jobId:resumeHash
    public static final String AI_MATCH_BYPASS    = "ai:match:bypass";     // key = userId:jobId:resumeHash (marker skip 1 lần)
}
