package com.example.xiaoy.data

/**
 * 示例数据 —— 使用者故事：
 * 「悦悦妈」记录女儿「小满」（满宝，2020 年 3 月出生，6 岁半，幼儿园大班，9 月升小学）
 * 的城市生活。她每天用「小芽」记下小满的作息、阅读、学习、成长瞬间与亲子时光。
 * 所有日期均为相对「今天」动态生成，保证首次打开即是新鲜、连贯的真实工作台。
 */
object SampleData {

    private fun r(
        id: String, type: RecordType, title: String, subtitle: String, date: Long,
        status: RecordStatus, tags: List<String> = emptyList(), notes: String = "",
        images: List<String> = emptyList(), num1: Double? = null, num2: Double? = null,
        text1: String = "", location: String = "", endDate: Long? = null, createdAgoDays: Int = 0
    ) = Record(
        id = id, type = type.id, title = title, subtitle = subtitle, dateEpoch = date,
        endDateEpoch = endDate, status = status.id, tags = tags, notes = notes,
        images = images, num1 = num1, num2 = num2, text1 = text1, location = location,
        createdAt = daysAgo(createdAgoDays)
    )

    fun seed(): AppData {
        val t = startOfToday()
        val records = listOf(
            // —— 作息 ——
            r("r01", RecordType.ROUTINE, "早睡早起 · 21 天打卡", "连续打卡计划", t,
                RecordStatus.DOING, listOf("习惯"), "今晚 21:30 前入睡，明早 7:00 起床，已经坚持两周啦。",
                listOf(ImageRef.of("img_sticker")), num1 = 14.0, text1 = "起床 07:20 · 入睡 21:30"),
            r("r02", RecordType.ROUTINE, "自主洗漱", "晚间作息", t,
                RecordStatus.DONE, listOf("习惯"), "今天自己刷牙洗脸、换睡衣，一次都没催。",
                num1 = 1.0),

            // —— 阅读 ——
            r("r03", RecordType.READING, "《月亮的味道》睡前共读", "绘本 · 亲子共读", t,
                RecordStatus.DONE, listOf("亲子", "绘本"), "读到一半满宝抢着猜下一页，猜对了咯咯笑。",
                listOf(ImageRef.of("img_cover_moon")), num1 = 32.0, num2 = 20.0),
            r("r04", RecordType.READING, "《青蛙和蟾蜍》自主阅读", "桥梁书 · 自主阅读", daysAgo(1),
                RecordStatus.DOING, listOf("自主阅读"), "能自己拼读大半页了，遇到不会的会先猜再问我。",
                listOf(ImageRef.of("img_cover_frog")), num1 = 18.0, num2 = 15.0),

            // —— 学习任务 ——
            r("r05", RecordType.STUDY, "拼音拼读 · 声母复习", "幼小衔接", t,
                RecordStatus.TODO, listOf("幼小衔接"), "重点复习 b/p、d/t 容易混淆的几组。",
                listOf(ImageRef.of("img_pinyin")), num1 = 15.0),
            r("r06", RecordType.STUDY, "10 以内加减法口算", "数学练习", daysAgo(1),
                RecordStatus.DONE, listOf("数学"), "5 分钟做完 20 题，全对，奖励了一枚贴纸。",
                num1 = 20.0, num2 = 5.0),
            r("r18", RecordType.STUDY, "整理书包 · 认识课程表", "开学准备", t,
                RecordStatus.TODO, listOf("开学"), "先认一认星期几上什么课，练两遍摆放书本的位置。",
                num1 = 10.0),

            // —— 成长记录 ——
            r("r07", RecordType.GROWTH, "第一次自己系鞋带", "成长里程碑", daysAgo(2),
                RecordStatus.DONE, listOf("里程碑"), "在门口鼓捣了十分钟，终于成功，得意得跳起来。",
                listOf(ImageRef.of("img_tree"))),
            r("r08", RecordType.GROWTH, "主动帮奶奶浇花", "成长瞬间", daysAgo(3),
                RecordStatus.DONE, listOf("暖心"), "看见奶奶拎水壶，自己接过去给小阳台的花都浇了一遍。",
                listOf(ImageRef.of("img_garden"))),

            // —— 身高体重 ——
            r("r09", RecordType.BODY, "8 月体检 · 身高体重", "社区体检", daysAgo(1),
                RecordStatus.DONE, listOf("体检"), "比上个月长了 0.7 厘米，医生说长势很好。",
                listOf(ImageRef.of("img_height")), num1 = 118.5, num2 = 21.2),
            r("r10", RecordType.BODY, "7 月体检 · 身高体重", "社区体检", daysAgo(32),
                RecordStatus.DONE, listOf("体检"), "夏天胃口一般，体重略轻，注意补钙。",
                listOf(ImageRef.of("img_height")), num1 = 117.8, num2 = 20.6),

            // —— 兴趣课 ——
            r("r11", RecordType.INTEREST, "少儿芭蕾启蒙", "舞蹈课程", daysAgo(2),
                RecordStatus.DOING, listOf("兴趣课"), "这周学了新的把杆动作，回家演示给我看。",
                listOf(ImageRef.of("img_ballet")), text1 = "周三 / 周六 16:00", location = "家门口舞蹈教室"),
            r("r12", RecordType.INTEREST, "创意美术课", "绘画课程", daysAgo(6),
                RecordStatus.DOING, listOf("兴趣课"), "这周画了一幅《彩虹房子》，贴在冰箱上了。",
                listOf(ImageRef.of("img_art_child")), text1 = "周六 10:30", location = "邻里美术工作室"),

            // —— 亲子活动 ——
            r("r13", RecordType.ACTIVITY, "一起做南瓜饼", "亲子厨房", daysAgo(4),
                RecordStatus.DONE, listOf("亲子", "食育"), "满宝负责搓圆，还偷偷尝了一口南瓜泥。",
                listOf(ImageRef.of("img_pumpkin")), location = "家里厨房"),
            r("r15", RecordType.ACTIVITY, "公园放风筝", "户外撒欢", daysAgo(6),
                RecordStatus.DONE, listOf("亲子", "户外"), "风不大，但满宝拉着线跑了一下午，开心极了。",
                listOf(ImageRef.of("img_garden")), location = "滨江公园"),
            r("r14", RecordType.ACTIVITY, "周末植物园踏青", "亲子出游", t,
                RecordStatus.PLANNED, listOf("亲子", "户外"), "带上水杯和小零食，看看向日葵开得怎么样。",
                listOf(ImageRef.of("img_garden")), location = "杭州植物园", endDate = daysFromNow(2)),
            r("r19", RecordType.ACTIVITY, "开学前野餐", "亲子出游", t,
                RecordStatus.PLANNED, listOf("亲子", "户外"), "开学前的最后一场野餐，约好了要带上泡泡机。",
                listOf(ImageRef.of("img_garden")), location = "湘湖草坪", endDate = daysFromNow(5)),

            // —— 物品清单 ——
            r("r16", RecordType.ITEM, "开学文具清单", "开学准备", t,
                RecordStatus.ATTENTION, listOf("开学"), "还差 3 项：橡皮 2 块、姓名贴、小雨衣。",
                num1 = 12.0, num2 = 9.0),
            r("r17", RecordType.ITEM, "夏季衣物收纳", "换季整理", daysAgo(5),
                RecordStatus.DONE, listOf("收纳"), "把穿不下的短袖叠好装箱，腾出了半个抽屉。",
                num1 = 1.0)
        )

        return AppData(
            version = 1,
            profile = ChildProfile(
                parentName = "悦悦妈",
                childName = "小满",
                childBirthday = "2020-03-12",
                city = "杭州",
                motto = "把孩子的每一次长大，温柔记下来"
            ),
            records = records,
            customTags = listOf("习惯", "亲子", "绘本", "自主阅读", "幼小衔接", "数学", "里程碑", "暖心", "体检", "兴趣课", "食育", "户外", "开学", "收纳"),
            reminderEnabled = true,
            reminderTime = "20:30"
        )
    }
}
