package com.example.data.localization

object NivoStrings {
    // Navigation
    fun navHome(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "হোম" else "Home"
    fun navCalendar(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ক্যালেন্ডার" else "Calendar"
    fun navGoals(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "লক্ষ্য" else "Goals"
    fun navAlarms(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অ্যালার্ম" else "Alarms"
    fun navSettings(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সেটিংস" else "Settings"

    // Greetings
    fun greeting(lang: AppLanguage, name: String?): String {
        return if (lang == AppLanguage.BANGLA) {
            if (name.isNullOrBlank()) "আবার স্বাগতম!" else "আবার স্বাগতম, $name!"
        } else {
            if (name.isNullOrBlank()) "Welcome back!" else "Welcome back, $name!"
        }
    }

    fun appTagline(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "প্রতিদিন এক ধাপ এগিয়ে।" else "One Day at a Time."
    fun homeSubtitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "নিজের শৃঙ্খলায় অবিচল থাকুন।" else "Build discipline, one day at a time."

    // Check In & Countdowns
    fun checkInButton(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "চেক ইন" else "Check In"
    fun checkedInForToday(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আজকের চেক-ইন সম্পন্ন" else "Checked In for Today"
    fun checkInReady(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আজকের চেক-ইন প্রস্তুত। উপরে ট্যাপ করুন!" else "Today's check-in is ready. Tap Check In above!"
    fun nextCheckInIn(lang: AppLanguage, hours: Long, minutes: Long, seconds: Long): String {
        return if (lang == AppLanguage.BANGLA) {
            "পরবর্তী চেক-ইন বাকি: ${hours.toLocalizedDigits(lang)} ঘণ্টা ${minutes.toLocalizedDigits(lang)} মি. ${seconds.toLocalizedDigits(lang)} সে."
        } else {
            "Next check-in in: ${hours}h ${minutes}m ${seconds}s"
        }
    }
    fun nextMidnightResetIn(lang: AppLanguage, hours: Long, minutes: Long, seconds: Long): String {
        return if (lang == AppLanguage.BANGLA) {
            "রাত ১২:০০ টায় রিসেট হবে: ${hours.toLocalizedDigits(lang)} ঘণ্টা ${minutes.toLocalizedDigits(lang)} মি. ${seconds.toLocalizedDigits(lang)} সে."
        } else {
            "Resets at 12:00 AM in: ${hours}h ${minutes}m ${seconds}s"
        }
    }

    // Progress
    fun manualProgressTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ম্যানুয়াল অগ্রগতি" else "Manual Progress"
    fun automaticProgressTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "স্বয়ংক্রিয় অগ্রগতি" else "Automatic Progress"
    fun manualProgressDesc(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "চেক-ইন বোতামের মাধ্যমে অর্জিত" else "Recorded via manual Check In"
    fun automaticProgressDesc(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "প্রতি ২৪ ঘণ্টা সময়ের সাথে বৃদ্ধি পায়" else "Advances automatically every 24 hours"

    // Tracking Modes (Option A, Option B & Option C)
    fun trackingModeLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ট্র্যাকিং মোড" else "Tracking Mode"
    fun optionAManual(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অপশন এ: ম্যানুয়াল ট্র্যাকিং" else "Option A: Manual Tracking"
    fun optionBAutomatic(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অপশন বি: স্বয়ংক্রিয় ট্র্যাকিং" else "Option B: Automatic Tracking"
    fun optionCDailyReset(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অপশন সি: ডেইলি রিসেট ট্র্যাকিং" else "Option C: Daily Reset Tracking"
    fun manualModeShort(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ম্যানুয়াল" else "Manual Tracking"
    fun autoModeShort(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "স্বয়ংক্রিয়" else "Automatic Tracking"
    fun dailyResetModeShort(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ডেইলি রিসেট" else "Daily Reset"
    fun manualModeDesc(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "প্রতি ২৪ ঘণ্টায় একবার চেক-ইন বাটন চেপে অগ্রগতি রেকর্ড করতে হবে।" else "Press the Check-in button once every 24 hours to record progress."
    fun autoModeDesc(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "কোনো বাটন চাপার প্রয়োজন নেই। প্রতি ২৪ ঘণ্টায় স্বয়ংক্রিয়ভাবে অগ্রগতি বাড়বে।" else "Calculates automatically from start time. Advances 1 day every 24 hours without check-in."
    fun dailyResetModeDesc(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "প্রতি ক্যালেন্ডার দিনে একবার চেক-ইন করা যায়। রাত ১২:০০ টা পর্যন্ত বাটন বন্ধ থাকবে এবং নতুন দিনে স্বয়ংক্রিয়ভাবে চালু হবে।" else "Check in once per calendar day. Locks until 12:00 AM midnight and automatically resets for the next day."
    fun autoModeActiveNotice(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "স্বয়ংক্রিয় মোড সক্রিয় — প্রতি ২৪ ঘণ্টায় অগ্রগতি স্বয়ংক্রিয়ভাবে বৃদ্ধি পায়।" else "Automatic Mode Active — Progress increases every 24 hours without check-in."
    fun nextAutoDayCountdown(lang: AppLanguage, hours: Long, minutes: Long, seconds: Long): String {
        return if (lang == AppLanguage.BANGLA) {
            "পরবর্তী স্বয়ংক্রিয় দিন বাকি: ${hours.toLocalizedDigits(lang)} ঘণ্টা ${minutes.toLocalizedDigits(lang)} মি. ${seconds.toLocalizedDigits(lang)} সে."
        } else {
            "Next automatic day in: ${hours}h ${minutes}m ${seconds}s"
        }
    }
    fun optionalManualCheckIn(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ঐচ্ছিক ম্যানুয়াল চেক-ইন" else "Optional Manual Check In"

    fun ofDays(lang: AppLanguage, completed: Int, total: Int): String {
        return if (lang == AppLanguage.BANGLA) {
            "${total.toLocalizedDigits(lang)} দিনের মধ্যে ${completed.toLocalizedDigits(lang)} দিন সম্পন্ন"
        } else {
            "$completed of $total Days"
        }
    }

    fun daysRatio(lang: AppLanguage, completed: Int, total: Int): String {
        return if (lang == AppLanguage.BANGLA) {
            "${completed.toLocalizedDigits(lang)}/${total.toLocalizedDigits(lang)} দিন"
        } else {
            "$completed/$total days"
        }
    }

    fun daysRemaining(lang: AppLanguage, remaining: Int): String {
        return if (lang == AppLanguage.BANGLA) {
            "${remaining.toLocalizedDigits(lang)} দিন বাকি"
        } else {
            "$remaining days remaining"
        }
    }

    // Empty States
    fun noActiveGoal(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "কোনো সক্রিয় লক্ষ্য নেই" else "No Active Goal Yet"
    fun noActiveGoalDesc(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "একটি লক্ষ্য নির্ধারণ করুন এবং প্রতিদিন শৃঙ্খলা গড়ে তুলুন।" else "Set a goal and build lifelong discipline one day at a time."
    fun createFirstGoal(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "প্রথম লক্ষ্য তৈরি করুন" else "Create Your First Goal"
    fun loadSampleGoal(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "নমুনা ৩০ দিনের লক্ষ্য লোড করুন" else "Load sample 30-day goal"

    // Goal Management
    fun myGoals(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আমার লক্ষ্যসমূহ" else "My Goals"
    fun newGoal(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "নতুন লক্ষ্য" else "New Goal"
    fun editGoal(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "লক্ষ্য সম্পাদনা" else "Edit Goal"
    fun activeOnHome(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "হোম স্ক্রিনে সক্রিয়" else "Active on Home Screen"
    fun startDateLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "শুরুর তারিখ" else "Start Date"
    fun durationLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "মেয়াদ" else "Duration"
    fun daysUnit(lang: AppLanguage, days: Int) = if (lang == AppLanguage.BANGLA) "${days.toLocalizedDigits(lang)} দিন" else "$days Days"
    fun completedLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সম্পন্ন" else "Completed"
    fun remainingLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অবশিষ্ট" else "Remaining"
    fun progressLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অগ্রগতি" else "Progress"
    fun expectedCompletionLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "প্রত্যাশিত সমাপ্তি" else "Target Date"
    fun streakLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ধারাবাহিকতা" else "Streak"

    // Form Fields & Validation
    fun goalTitleLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "লক্ষ্যের শিরোনাম *" else "Goal Title *"
    fun goalTitlePlaceholder(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "যেমন: ৩০ দিন নিয়মিত বই পড়া" else "e.g. 30 Days of Morning Workout"
    fun goalDurationLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "লক্ষ্যের মেয়াদ (২ থেকে ৩৬৫ দিন)" else "Goal Duration (2 to 365 Days)"
    fun personalReasonLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ব্যক্তিগত কারণ (ঐচ্ছিক)" else "Personal Reason (Optional)"
    fun personalReasonPlaceholder(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "এই লক্ষ্যটি আপনার কাছে কেন গুরুত্বপূর্ণ? কঠিন দিনে কী আপনাকে অনুপ্রাণিত রাখবে?" else "Why is this goal important to you? What will keep you disciplined on tough days?"
    fun saveGoalButton(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সংরক্ষণ করুন" else "Save Goal"
    fun updateGoalButton(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আপডেট করুন" else "Update Goal"
    fun cancelButton(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বাতিল" else "Cancel"
    fun deleteButton(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "মুছুন" else "Delete"

    // Validation Errors
    fun errorTitleRequired(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অনুগ্রহ করে লক্ষ্যের শিরোনাম লিখুন" else "Please enter a goal title"
    fun errorDurationInvalid(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "মেয়াদ অবশ্যই ২ থেকে ৩৬৫ দিনের মধ্যে পূর্ণসংখ্যা হতে হবে" else "Duration must be an integer between 2 and 365 days"
    fun errorDurationTooSmall(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "মেয়াদ কমপক্ষে ২ দিন হতে হবে" else "Duration must be at least 2 days"
    fun errorDurationTooLarge(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "মেয়াদ ৩৬৫ দিনের বেশি হতে পারে না" else "Duration cannot exceed 365 days"

    // Goal Completion
    fun goalCompletedTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "লক্ষ্য অর্জিত হয়েছে!" else "GOAL COMPLETED"
    fun goalCompletedMessage(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অসাধারণ অধ্যবসায়! আপনি প্রতিদিন এক ধাপ করে আপনার লক্ষ্য সফলভাবে সম্পন্ন করেছেন।" else "Outstanding perseverance! You stayed disciplined and completed your goal one day at a time."
    fun totalCheckInsLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "মোট চেক-ইন" else "Total Check-ins"
    fun completionDateLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সমাপ্তির তারিখ" else "Completion Date"
    fun returnToHomeButton(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "হোমে ফিরে যান" else "Return to Home"

    // Calendar
    fun progressCalendar(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অগ্রগতি ক্যালেন্ডার" else "Progress Calendar"
    fun calendarSubtitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "প্রতিদিনের ধারাবাহিকতা পর্যবেক্ষণ করুন।" else "Track your daily consistency one day at a time."
    fun completedStatus(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সম্পন্ন" else "Completed"
    fun missedStatus(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ছুটি / মিসড" else "Rest / Missed"
    fun todayStatus(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আজ" else "Today"
    fun upcomingStatus(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আসন্ন" else "Upcoming"
    fun monthlySummary(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "মাসিক সারাংশ" else "Monthly Summary"
    fun consistencyRate(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ধারাবাহিকতার হার" else "Consistency"

    // Settings
    fun settingsTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সেটিংস" else "Settings"
    fun settingsSubtitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "পছন্দসমূহ ও অ্যাপ বিবরণ" else "Preferences and application details"
    fun sectionPersonalization(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ব্যক্তিগতকরণ" else "Personalization"
    fun sectionDisplayTheme(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ডিসপ্লে ও থিম" else "Display & Theme"
    fun themeSelectionTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অ্যাপ থিম নির্বাচন" else "Select App Theme"
    fun themeSelectionSubtitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "লাইট থিম বা ডার্ক রেড থিম বেছে নিন" else "Choose between Light Theme and Dark Red Theme"
    fun themeLightName(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "লাইট থিম" else "Light Theme"
    fun themeLightDesc(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "পরিচ্ছন্ন অফ-হোয়াইট, প্যাস্টেল কার্ড ও সফট গ্রিন" else "Clean off-white surfaces with pastel cards and soft green accents"
    fun themeDarkRedName(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ডার্ক রেড থিম" else "Dark Red Theme"
    fun themeDarkRedDesc(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "গভীর ম্যাট ব্ল্যাক এবং ক্রিমসন এলইডি রেড" else "Matte black chassis with crimson red highlights"
    fun sectionLanguage(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ভাষা (Language)" else "Language"
    fun sectionSecurity(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "নিরাপত্তা ও নোটিফিকেশন" else "Security & Alerts"
    fun sectionData(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "তথ্য ও গোপনীয়তা" else "Data & Privacy"
    fun sectionAbout(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "পরিচিতি" else "About"

    fun yourName(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আপনার নাম" else "Your Name"
    fun updateName(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "নাম পরিবর্তন" else "Update Name"
    fun activeGoalBadge(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সক্রিয়" else "Active"
    fun switchGoalTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সক্রিয় লক্ষ্য পরিবর্তন করুন" else "Switch Active Goal"
    fun appLockTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ঐচ্ছিক অ্যাপ লক" else "Optional App Lock"
    fun appLockSubtitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ডিভাইস লকের মাধ্যমে লক্ষ্য সুরক্ষিত রাখুন" else "Protect goal logs with device lock"
    fun reminderTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "নোটিফিকেশন ও রিমাইন্ডার" else "Notifications & Reminders"
    fun reminderSubtitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "দৈনিক অভ্যাস সতর্কবার্তা চালু বা বন্ধ করুন" else "Daily habit alerts and reminder controls"
    fun alarmSettingsTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অ্যালার্ম সেটিংস" else "Alarm Settings"
    fun alarmSettingsSubtitle(lang: AppLanguage, enabledCount: Int): String {
        return if (lang == AppLanguage.BANGLA) {
            "সক্রিয় অ্যালার্ম: ${enabledCount.toLocalizedDigits(lang)}টি • সাউন্ড ও সময়সূচী পরিচালনা"
        } else {
            "$enabledCount active • Manage alarm schedule & sounds"
        }
    }
    fun privacyTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "গোপনীয়তা" else "Privacy"
    fun privacySubtitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "১০০% অফলাইন। কোনো ট্র্যাকিং বা টেলিমিতি নেই।" else "100% Offline. Zero telemetry & no tracking."
    fun dataManagementTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ডেটা রিসেট করুন" else "Reset Local Data"
    fun dataManagementSubtitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সংরক্ষিত সমস্ত লক্ষ্য ও ইতিহাস মুছে ফেলুন" else "Clear all offline habits and check-ins"
    fun aboutNivo(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "নিভো সম্পর্কে" else "About NIVO"
    fun aboutDeveloper(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ডেভেলপার পরিচিতি" else "About Developer"

    // Dashboard & Hardware Telemetry
    fun dailyOverviewTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "দৈনিক অগ্রগতি" else "Daily Overview"
    fun totalHabitsLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "মোট অভ্যাস" else "Total Habits"
    fun completedHabitsLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সম্পন্ন" else "Completed"
    fun remainingHabitsLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বাকি আছে" else "Remaining"
    fun quickAccessTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "দ্রুত অ্যাক্সেস" else "Quick Access"
    fun upcomingRemindersTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আসন্ন অ্যালার্ম" else "Upcoming Alarms"
    fun noUpcomingAlarms(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "কোনো সক্রিয় অ্যালার্ম নেই" else "No active alarms scheduled"
    fun manageAlarmsAction(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সব দেখুন" else "Manage"
    fun alarmOnLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অ্যালার্ম চালু" else "Alarm ON"
    fun alarmOffLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অ্যালার্ম বন্ধ" else "Alarm OFF"
    fun noAlarmSetLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "কোনো অ্যালার্ম নেই" else "No Alarm"
    fun addAlarmQuickLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "+ অ্যালার্ম" else "+ Alarm"
    fun habitsStatusForDay(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "দিনের অভ্যাস বিবরণী" else "Habits on This Date"
    fun incompleteHabitsLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অসম্পূর্ণ" else "Incomplete"

    // Developer Screen
    fun devName(lang: AppLanguage) = "MD. MOJAHIDUR RAHMAN SAJID"
    fun devDepartmentLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বিভাগ" else "Department"
    fun devDepartmentValue(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "কম্পিউটার সায়েন্স অ্যান্ড টেকনোলজি" else "Computer Science & Technology"
    fun devInstitutionLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "প্রতিষ্ঠান" else "Institution"
    fun devInstitutionValue(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বগুড়া পলিটেকনিক ইনস্টিটিউট" else "Bogura Polytechnic Institute"
    fun devLocationLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অবস্থান" else "Location"
    fun devLocationValue(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বগুড়া, বাংলাদেশ" else "Bogura, Bangladesh"
    fun devPurposeTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "নিভো তৈরির উদ্দেশ্য" else "The Purpose of NIVO"
    fun devPurposeText(lang: AppLanguage): String {
        return if (lang == AppLanguage.BANGLA) {
            "নিভো তৈরি করা হয়েছে মানুষকে তাদের লক্ষ্যের প্রতি অবিচল থাকতে, ধীরে ধীরে শৃঙ্খলা গড়ে তুলতে এবং প্রতিদিন এক ধাপ করে এগিয়ে যাওয়ার জন্য সাহায্য করতে।\n\n" +
            "আসল সাফল্য হঠাৎ কোনো আবেগে আসে না; এটি আসে প্রতিদিনের বিনম্র ও অবিচল প্রতিশ্রুতির মাধ্যমে। সব ধরনের বিভ্রান্তি, বিজ্ঞাপন ও অনলাইন কোলাহল দূর করে নিভো একটি শান্ত, সম্পূর্ণ ব্যক্তিগত পরিবেশ প্রদান করে যেখানে আপনার পূর্ণ মনোযোগ থাকে কেবল আজকের দিনের দায়িত্বে।"
        } else {
            "NIVO was created to help people stay consistent with their goals, build discipline gradually, and focus on one day at a time.\n\n" +
            "Real growth rarely happens through sudden bursts of motivation; it emerges from modest, unwavering daily commitments. By removing all unnecessary noise, intrusive tracking, and online distractions, NIVO offers a calm, private sanctuary where your daily focus remains purely on the present day's progress."
        }
    }

    // Dialogs
    fun selectLanguageTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ভাষা নির্বাচন করুন" else "Select Language"
    fun deleteGoalConfirmTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "লক্ষ্য মুছে ফেলবেন?" else "Delete Goal?"
    fun deleteGoalConfirmMessage(lang: AppLanguage, title: String) = if (lang == AppLanguage.BANGLA) "আপনি কি নিশ্চিতভাবে \"$title\" মুছে ফেলতে চান? এই কাজটি পুনরায় ফিরিয়ে আনা যাবে না।" else "Are you sure you want to delete \"$title\"? This action cannot be undone."
    fun resetConfirmTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অ্যাপের সমস্ত ডেটা রিসেট করবেন?" else "Reset Application Data?"
    fun resetConfirmMessage(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "এটি আপনার সংরক্ষিত সকল লক্ষ্য এবং চেক-ইনের ইতিহাস মুছে ফেলবে। আপনি কি নিশ্চিত?" else "This will clear all created goals and check-in history from local storage. Are you sure?"
    fun doneButton(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সম্পন্ন" else "Done"

    // PIN Lock
    fun enterPinTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "পিন কোড লিখুন" else "Enter PIN"
    fun enterPinSubtitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "নিভো আনলক করতে আপনার ৪-সংখ্যার পিন দিন" else "Enter your 4-digit PIN to unlock NIVO"
    fun setPinTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "নতুন পিন কোড সেট করুন" else "Set New PIN"
    fun setPinSubtitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "যেকোনো ৪-সংখ্যার পিন নির্বাচন করুন" else "Choose any 4-digit PIN"
    fun confirmPinTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "পিন নিশ্চিত করুন" else "Confirm PIN"
    fun confirmPinSubtitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "নিশ্চিত করতে একই পিন পুনরায় লিখুন" else "Re-enter the same 4-digit PIN to confirm"
    fun disablePinTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বর্তমান পিন দিন" else "Enter Current PIN"
    fun disablePinSubtitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অ্যাপ লক বন্ধ করতে বর্তমান পিন দিন" else "Enter current PIN to disable App Lock"
    fun errorPinMismatch(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "পিন মেলেনি, আবার চেষ্টা করুন" else "PIN does not match, please try again"
    fun errorIncorrectPin(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ভুল পিন কোড" else "Incorrect PIN"
    fun pinLockEnabledSuccess(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অ্যাপ লক সক্রিয় করা হয়েছে" else "App Lock has been enabled"
    fun pinLockDisabledSuccess(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অ্যাপ লক নিষ্ক্রিয় করা হয়েছে" else "App Lock has been disabled"
    fun changePin(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "পিন পরিবর্তন করুন" else "Change PIN"

    // Backup & Restore
    fun exportDataTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ডেটা ব্যাকআপ (এক্সপোর্ট)" else "Data Backup (Export)"
    fun exportDataDesc(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আপনার অফলাইন লক্ষ্য এবং রেকর্ডের ব্যাকআপ টেক্সট কপি করুন" else "Copy your offline goals and check-in records as backup text"
    fun copyToClipboard(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ক্লিপবোর্ডে কপি করুন" else "Copy to Clipboard"
    fun copiedSuccess(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "কপি করা হয়েছে!" else "Copied to clipboard!"
    fun restoreDataTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ডেটা রিস্টোর (ইম্পোর্ট)" else "Data Restore (Import)"
    fun restoreDataDesc(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "পূর্বের ব্যাকআপ JSON পেস্ট করে লক্ষ্য ও রেকর্ড পুনরুদ্ধার করুন" else "Paste exported backup JSON to restore habits and check-in records"
    fun restoreInputPlaceholder(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "এখানে ব্যাকআপ JSON টেক্সট পেস্ট করুন..." else "Paste NIVO backup JSON here..."
    fun restoreButton(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "রিস্টোর করুন" else "Restore Backup"
    fun restoreSuccess(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ব্যাকআপ সফলভাবে রিস্টোর হয়েছে!" else "Backup restored successfully!"
    fun restoreFailed(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অবৈধ ব্যাকআপ ফরম্যাট। অনুগ্রহ করে সঠিক JSON দিন।" else "Invalid backup format. Please paste valid NIVO JSON."

    // Alarm System
    fun alarmsTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অ্যালার্ম" else "Alarms"
    fun alarmsSubtitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "স্বতন্ত্র ও অভ্যাসের অ্যালার্ম পরিচালনা করুন" else "Manage standalone and habit-linked alarms"
    fun myAlarmsSection(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আমার অ্যালার্ম" else "My Alarms"
    fun myAlarmsSubtitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "স্বতন্ত্র অ্যালার্ম" else "Standalone alarms"
    fun habitAlarmsSection(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অভ্যাসের অ্যালার্ম" else "Habit Alarms"
    fun habitAlarmsSubtitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অভ্যাসের সাথে সংযুক্ত অ্যালার্ম" else "Alarms connected to Habits"
    fun newAlarmButton(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "নতুন অ্যালার্ম" else "New Alarm"
    fun editAlarmTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অ্যালার্ম সম্পাদনা" else "Edit Alarm"
    fun alarmNameLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অ্যালার্মের নাম" else "Alarm Name"
    fun alarmNamePlaceholder(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "যেমন: সকালের ব্যায়াম" else "e.g. Morning Wakeup"
    fun alarmTimeLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সময় নির্বাচন করুন" else "Alarm Time"
    fun format12Hour(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "১২-ঘণ্টা (AM/PM)" else "12-Hour (AM/PM)"
    fun format24Hour(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "২৪-ঘণ্টা" else "24-Hour"
    fun repeatDaysLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "পুনরাবৃত্তির দিন" else "Repeat Days"
    fun snoozeDurationLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "স্নুজ সময়" else "Snooze"
    fun alarmSoundLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অ্যালার্ম সাউন্ড" else "Alarm Sound"
    fun previewSound(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "শুনুন" else "Preview"
    fun stopPreview(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "থামান" else "Stop"
    fun saveAlarmButton(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অ্যালার্ম সংরক্ষণ করুন" else "Save Alarm"
    fun deleteAlarmConfirmTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অ্যালার্ম মুছে ফেলবেন?" else "Delete Alarm?"
    fun deleteAlarmConfirmMessage(lang: AppLanguage, title: String) = if (lang == AppLanguage.BANGLA) "আপনি কি \"$title\" অ্যালার্মটি মুছে ফেলতে চান?" else "Are you sure you want to delete \"$title\"?"
    fun standaloneAlarmLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "স্বতন্ত্র অ্যালার্ম" else "Standalone Alarm"
    fun habitAlarmLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অভ্যাস অ্যালার্ম" else "Habit Alarm"
    fun habitReminderAlarmSection(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অভ্যাস রিমাইন্ডার / অ্যালার্ম" else "Habit Reminder / Alarm"
    fun enableHabitAlarmLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অভ্যাসের অ্যালার্ম চালু করুন" else "Enable Habit Alarm"
    fun habitAlarmOptionalDesc(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ঐচ্ছিক: অভ্যাস সম্পন্ন হলেও বন্ধ না করা পর্যন্ত চলতে থাকবে" else "Optional: keeps ringing on scheduled days until disabled or habit deleted"
    fun noStandaloneAlarms(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "এখনও কোনো স্বতন্ত্র অ্যালার্ম নেই" else "No standalone alarms yet"
    fun noHabitAlarms(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "কোনো অভ্যাসের সাথে অ্যালার্ম যুক্ত নেই। লক্ষ্য তৈরি বা সম্পাদনার সময় অ্যালার্ম যোগ করুন।" else "No habit alarms set yet. Enable one when creating or editing a Habit."
    fun dismissAlarmButton(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বন্ধ করুন" else "Dismiss"
    fun snoozeMinutesButton(lang: AppLanguage, min: Int) = if (lang == AppLanguage.BANGLA) "স্নুজ (${min.toLocalizedDigits(lang)} মি.)" else "Snooze ${min}m"
    fun everyDayLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "প্রতিদিন" else "Every day"
    fun oneTimeLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "একবার" else "Once"
    fun weekdaysLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "কর্মদিবস" else "Weekdays"
    fun weekendsLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সাপ্তাহিক ছুটি" else "Weekends"
    fun habitCompletedStillActiveBadge(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অভ্যাস সম্পন্ন • অ্যালার্ম সক্রিয়" else "Habit Completed • Alarm Active"
    fun notificationPermissionNeeded(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অ্যালার্ম প্রদর্শনের জন্য নোটিফিকেশন অনুমতি প্রয়োজন" else "Notification permission required for alarm alerts"
    fun exactAlarmPermissionNeeded(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সঠিক সময়ে অ্যালার্ম বাজতে এক্সাক্ট অ্যালার্ম অনুমতি দিন" else "Allow exact alarms so reminders ring precisely on time"
    fun grantPermissionButton(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অনুমতি দিন" else "Allow"
    fun alarmSoundErrorTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সাউন্ড" else "Sound"
    fun selectHabitForAlarm(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অভ্যাস নির্বাচন করুন" else "Link Habit Alarm"

    fun dayShortLabel(dayOfWeek: Int, lang: AppLanguage): String {
        return if (lang == AppLanguage.BANGLA) {
            when (dayOfWeek) {
                java.util.Calendar.SUNDAY -> "রবি"
                java.util.Calendar.MONDAY -> "সোম"
                java.util.Calendar.TUESDAY -> "মঙ্গল"
                java.util.Calendar.WEDNESDAY -> "বুধ"
                java.util.Calendar.THURSDAY -> "বৃহঃ"
                java.util.Calendar.FRIDAY -> "শুক্র"
                java.util.Calendar.SATURDAY -> "শনি"
                else -> ""
            }
        } else {
            when (dayOfWeek) {
                java.util.Calendar.SUNDAY -> "Sun"
                java.util.Calendar.MONDAY -> "Mon"
                java.util.Calendar.TUESDAY -> "Tue"
                java.util.Calendar.WEDNESDAY -> "Wed"
                java.util.Calendar.THURSDAY -> "Thu"
                java.util.Calendar.FRIDAY -> "Fri"
                java.util.Calendar.SATURDAY -> "Sat"
                else -> ""
            }
        }
    }

    fun formatRepeatDaysSummary(days: Set<Int>, isHabit: Boolean, lang: AppLanguage): String {
        val effective = if (isHabit && days.isEmpty()) com.example.data.model.AlarmItem.ALL_DAYS else days
        return when {
            effective.isEmpty() -> oneTimeLabel(lang)
            effective.size == 7 -> everyDayLabel(lang)
            effective == com.example.data.model.AlarmItem.WEEKDAYS -> weekdaysLabel(lang)
            effective == com.example.data.model.AlarmItem.WEEKENDS -> weekendsLabel(lang)
            else -> com.example.data.model.AlarmItem.ORDERED_DAYS
                .filter { it in effective }
                .joinToString(", ") { dayShortLabel(it, lang) }
        }
    }
}
