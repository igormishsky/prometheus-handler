package com.cyclesync.ui.content

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class ContentArticle(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val readingTimeMinutes: Int,
    val sections: List<ContentSection>
)

data class ContentSection(
    val heading: String? = null,
    val body: String
)

@Composable
fun ContentScreen() {
    val articles = remember { getBundledArticles() }
    var selectedArticle by remember { mutableStateOf<ContentArticle?>(null) }

    if (selectedArticle != null) {
        ArticleDetailScreen(
            article = selectedArticle!!,
            onBack = { selectedArticle = null }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Learn",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            articles.groupBy { it.category }.forEach { (category, categoryArticles) ->
                Text(
                    text = category.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                categoryArticles.forEach { article ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedArticle = article },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = article.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = article.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${article.readingTimeMinutes} min read",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ArticleDetailScreen(
    article: ContentArticle,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "< Back",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { onBack() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = article.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = article.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        article.sections.forEach { section ->
            section.heading?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Text(
                text = section.body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

private fun getBundledArticles(): List<ContentArticle> = listOf(
    ContentArticle(
        id = "understanding_cycle",
        title = "Understanding Your Menstrual Cycle",
        subtitle = "The four phases and what they mean for your body",
        category = "cycle basics",
        readingTimeMinutes = 5,
        sections = listOf(
            ContentSection(
                heading = "The Four Phases",
                body = "Your menstrual cycle has four distinct phases: menstruation, the follicular phase, ovulation, and the luteal phase. Each phase is driven by different hormones and affects your body differently."
            ),
            ContentSection(
                heading = "Menstruation (Days 1-5)",
                body = "The cycle begins with menstruation \u2014 the shedding of the uterine lining. This typically lasts 3-7 days. Day 1 of your period is Day 1 of your cycle."
            ),
            ContentSection(
                heading = "Follicular Phase (Days 1-13)",
                body = "Overlapping with menstruation, the follicular phase is when your body prepares an egg for release. Estrogen rises, and you may feel increasing energy and improved mood."
            ),
            ContentSection(
                heading = "Ovulation (Day ~14)",
                body = "A mature egg is released from the ovary. This is the most fertile time of your cycle. The egg survives about 12-24 hours, but sperm can survive up to 5 days, creating a fertile window of about 6 days."
            ),
            ContentSection(
                heading = "Luteal Phase (Days 15-28)",
                body = "After ovulation, progesterone rises to prepare the uterus for a potential pregnancy. If the egg is not fertilized, hormone levels drop, triggering menstruation and the start of a new cycle."
            ),
            ContentSection(
                heading = "Normal Variation",
                body = "The average cycle is 29.3 days (Bull et al., 2019), but anywhere from 21-35 days is considered normal. Most variation comes from the follicular phase \u2014 the luteal phase is relatively stable at about 12-14 days."
            )
        )
    ),
    ContentArticle(
        id = "what_is_ovulation",
        title = "What Is Ovulation?",
        subtitle = "Understanding the key event in your cycle",
        category = "cycle basics",
        readingTimeMinutes = 4,
        sections = listOf(
            ContentSection(
                heading = "The Process",
                body = "Ovulation occurs when a mature egg is released from one of your ovaries. This is triggered by a surge in luteinizing hormone (LH). The egg then travels down the fallopian tube where it may be fertilized."
            ),
            ContentSection(
                heading = "Signs of Ovulation",
                body = "Common signs include: changes in cervical mucus (becomes clear, stretchy, and egg-white in consistency), a slight rise in basal body temperature (BBT), mild pelvic pain or twinges (mittelschmerz), and increased libido."
            ),
            ContentSection(
                heading = "Tracking Ovulation",
                body = "You can track ovulation using BBT charting (temperature rises 0.2-0.5 degrees after ovulation), ovulation predictor kits (OPKs) that detect the LH surge, or by monitoring cervical mucus changes."
            )
        )
    ),
    ContentArticle(
        id = "pms_explained",
        title = "PMS Explained",
        subtitle = "What causes premenstrual symptoms and how to manage them",
        category = "cycle basics",
        readingTimeMinutes = 5,
        sections = listOf(
            ContentSection(
                heading = "What Is PMS?",
                body = "Premenstrual syndrome (PMS) refers to physical and emotional symptoms that occur in the 1-2 weeks before your period. Up to 75% of menstruating people experience some form of PMS."
            ),
            ContentSection(
                heading = "Common Symptoms",
                body = "Physical: bloating, breast tenderness, headaches, fatigue, cramps, acne, appetite changes. Emotional: mood swings, irritability, anxiety, sadness, difficulty concentrating."
            ),
            ContentSection(
                heading = "Why It Happens",
                body = "PMS is linked to the drop in estrogen and progesterone levels that occurs after ovulation when pregnancy does not occur. The exact mechanism is not fully understood, but it involves the interaction between hormones and brain neurotransmitters like serotonin."
            ),
            ContentSection(
                heading = "Management",
                body = "Regular exercise, adequate sleep, stress management, and a balanced diet can help. Some people find relief with calcium, magnesium, or vitamin B6 supplements. Severe PMS (PMDD) may require medical treatment."
            )
        )
    ),
    ContentArticle(
        id = "fertile_window",
        title = "The Fertile Window",
        subtitle = "When conception is most likely",
        category = "fertility",
        readingTimeMinutes = 4,
        sections = listOf(
            ContentSection(
                heading = "The 6-Day Window",
                body = "The fertile window spans approximately 6 days: the 5 days before ovulation and the day of ovulation itself. This is because sperm can survive in the reproductive tract for up to 5 days, while the egg survives only 12-24 hours."
            ),
            ContentSection(
                heading = "Peak Fertility Days",
                body = "Based on research by Wilcox et al. (2000, NEJM), the highest probability of conception is on the 2 days before ovulation (approximately 27-29% per day) and the day before ovulation (27%)."
            ),
            ContentSection(
                heading = "Identifying Your Window",
                body = "CycleSync estimates your fertile window based on your cycle history and predicted ovulation date. For more precision, combine cycle tracking with BBT charting and cervical mucus observation."
            )
        )
    ),
    ContentArticle(
        id = "bbt_guide",
        title = "BBT Charting Guide",
        subtitle = "How to track your basal body temperature",
        category = "fertility",
        readingTimeMinutes = 5,
        sections = listOf(
            ContentSection(
                heading = "What Is BBT?",
                body = "Basal body temperature is your lowest body temperature in a 24-hour period, typically measured first thing in the morning before any activity. After ovulation, BBT rises by about 0.2-0.5 degrees Celsius due to progesterone."
            ),
            ContentSection(
                heading = "How to Measure",
                body = "Use a thermometer accurate to 0.1 degrees. Measure at the same time each morning, before getting out of bed, eating, or drinking. Record the temperature immediately."
            ),
            ContentSection(
                heading = "Reading Your Chart",
                body = "Look for a biphasic pattern: lower temperatures before ovulation (follicular phase) and higher temperatures after (luteal phase). The shift confirms ovulation occurred, but only retrospectively \u2014 BBT cannot predict ovulation in advance."
            ),
            ContentSection(
                heading = "Factors That Affect BBT",
                body = "Illness, alcohol, poor sleep, travel across time zones, and irregular sleep schedules can all affect BBT readings. Flag any disturbed readings in the app so they can be excluded from analysis."
            )
        )
    ),
    ContentArticle(
        id = "perimenopause",
        title = "Understanding Perimenopause",
        subtitle = "What to expect during the menopausal transition",
        category = "perimenopause",
        readingTimeMinutes = 6,
        sections = listOf(
            ContentSection(
                heading = "What Is Perimenopause?",
                body = "Perimenopause is the transition period leading to menopause. It typically begins in the mid-40s but can start in the late 30s. During this time, estrogen levels fluctuate unpredictably."
            ),
            ContentSection(
                heading = "Cycle Changes",
                body = "Cycles may become longer or shorter, heavier or lighter, or more irregular. You may skip periods entirely. These changes are normal but should be discussed with your healthcare provider."
            ),
            ContentSection(
                heading = "Common Symptoms",
                body = "Hot flashes, night sweats, sleep disturbances, mood changes, vaginal dryness, decreased libido, brain fog, joint pain, and heart palpitations are all common during perimenopause."
            ),
            ContentSection(
                heading = "When It Ends",
                body = "Menopause is defined as 12 consecutive months without a period. The average age of menopause is 51. CycleSync tracks your months without a period to help you monitor this transition."
            )
        )
    ),
    ContentArticle(
        id = "cycle_irregularity",
        title = "When Cycles Are Irregular",
        subtitle = "Causes and when to seek medical advice",
        category = "conditions",
        readingTimeMinutes = 5,
        sections = listOf(
            ContentSection(
                heading = "What Is Irregular?",
                body = "A cycle is considered irregular if it consistently falls outside the 21-35 day range, varies by more than 7-9 days cycle to cycle, or if periods are absent for more than 90 days (outside of pregnancy or menopause)."
            ),
            ContentSection(
                heading = "Common Causes",
                body = "Stress, significant weight changes, excessive exercise, polycystic ovary syndrome (PCOS), thyroid disorders, perimenopause, and certain medications can all cause irregular cycles."
            ),
            ContentSection(
                heading = "When to See a Doctor",
                body = "Consult a healthcare provider if: your period stops for more than 3 months, you bleed between periods, periods are extremely heavy or painful, cycles are consistently shorter than 21 days or longer than 35 days, or you experience sudden changes in your cycle pattern."
            )
        )
    ),
    ContentArticle(
        id = "pcos",
        title = "PCOS Overview",
        subtitle = "Understanding polycystic ovary syndrome",
        category = "conditions",
        readingTimeMinutes = 6,
        sections = listOf(
            ContentSection(
                heading = "What Is PCOS?",
                body = "Polycystic ovary syndrome is a hormonal condition affecting up to 10% of people with ovaries. It involves an imbalance of reproductive hormones that can affect ovulation, periods, and fertility."
            ),
            ContentSection(
                heading = "Common Symptoms",
                body = "Irregular or absent periods, heavy bleeding, excess hair growth (hirsutism), acne, weight gain (particularly around the midsection), thinning hair, and difficulty conceiving."
            ),
            ContentSection(
                heading = "Diagnosis",
                body = "PCOS is typically diagnosed when two of three criteria are met: irregular ovulation, elevated androgens (male hormones), and/or polycystic ovaries on ultrasound. It requires medical evaluation."
            ),
            ContentSection(
                heading = "Management",
                body = "Treatment depends on symptoms and goals. Options include lifestyle changes (diet, exercise), hormonal birth control, anti-androgen medications, and fertility treatments if trying to conceive."
            )
        )
    ),
    ContentArticle(
        id = "self_care_by_phase",
        title = "Self-Care by Cycle Phase",
        subtitle = "Tailor your wellness routine to your hormonal rhythm",
        category = "wellness",
        readingTimeMinutes = 6,
        sections = listOf(
            ContentSection(
                heading = "Why Phase-Based Self-Care?",
                body = "Your hormones fluctuate throughout your cycle, affecting your energy, mood, skin, and needs. By aligning your self-care practices with your cycle phase, you can work with your body instead of against it."
            ),
            ContentSection(
                heading = "Menstruation: Rest & Restore",
                body = "Prioritize rest, warmth, and nourishment. Try gentle yoga or stretching, warm baths with Epsom salts, journaling, and comfort foods rich in iron. This is your time to slow down and recharge. Face masks and cozy blankets are your best friends."
            ),
            ContentSection(
                heading = "Follicular: Explore & Create",
                body = "Energy is rising and so is your creativity. Try new workouts, start projects, experiment with new skincare products, or plan social activities. Your skin tends to be at its best, so it's a great time for lighter moisturizers and gentle exfoliation."
            ),
            ContentSection(
                heading = "Ovulation: Shine & Connect",
                body = "You're at peak energy and confidence. This is the best time for challenging workouts, important meetings, date nights, and social events. Your skin may glow naturally. Embrace this phase of radiance and connection."
            ),
            ContentSection(
                heading = "Luteal & PMS: Nurture & Soothe",
                body = "As progesterone rises and then drops, focus on calming activities. Meditation, aromatherapy, warm herbal teas, hydrating face masks, and gentle movement like walking or swimming are ideal. Be extra kind to yourself and honor your need for comfort."
            )
        )
    ),
    ContentArticle(
        id = "hydration_health",
        title = "Hydration & Your Cycle",
        subtitle = "Why water intake matters throughout your menstrual cycle",
        category = "wellness",
        readingTimeMinutes = 4,
        sections = listOf(
            ContentSection(
                heading = "Why Hydration Matters",
                body = "Water is essential for every bodily function, including hormone transport, nutrient delivery, and waste removal. During your menstrual cycle, your hydration needs change with your hormones."
            ),
            ContentSection(
                heading = "During Menstruation",
                body = "You lose fluid along with blood during your period. Increase your water intake and consider warm beverages like herbal tea or ginger tea to ease cramps and stay hydrated. Aim for at least 8 glasses per day."
            ),
            ContentSection(
                heading = "Follicular & Ovulation Phases",
                body = "Rising estrogen can increase your body's need for water. If you're exercising more during these high-energy phases, make sure to hydrate before, during, and after workouts. Coconut water is a great natural electrolyte source."
            ),
            ContentSection(
                heading = "Luteal Phase",
                body = "Progesterone can cause water retention and bloating. Paradoxically, drinking more water helps reduce bloating by signaling your body to release retained fluid. Limit caffeine and salty foods that worsen water retention."
            ),
            ContentSection(
                heading = "Hydrating Foods",
                body = "Cucumbers, watermelon, berries, oranges, lettuce, and celery are all over 90% water. Incorporating these into your diet is a delicious way to boost hydration alongside drinking water."
            )
        )
    ),
    ContentArticle(
        id = "skincare_cycle",
        title = "Skincare Through Your Cycle",
        subtitle = "How hormones affect your skin and how to adapt",
        category = "wellness",
        readingTimeMinutes = 5,
        sections = listOf(
            ContentSection(
                heading = "The Hormone-Skin Connection",
                body = "Your skin changes throughout your menstrual cycle due to fluctuating estrogen, progesterone, and testosterone levels. Understanding these changes helps you choose the right products at the right time."
            ),
            ContentSection(
                heading = "Menstruation (Days 1-5)",
                body = "Skin may be dry, dull, and sensitive as hormone levels are at their lowest. Focus on hydration: use rich moisturizers, gentle cleansers, and soothing ingredients like hyaluronic acid and aloe vera. Avoid harsh treatments."
            ),
            ContentSection(
                heading = "Follicular Phase (Days 6-13)",
                body = "Rising estrogen boosts collagen production and gives skin a natural glow. This is the best time for exfoliation, trying new products, and professional treatments like facials or chemical peels. Your skin is more resilient now."
            ),
            ContentSection(
                heading = "Ovulation (Day ~14)",
                body = "Estrogen peaks and skin looks its best \u2014 plump, glowing, and clear. Minimal skincare is needed. Focus on sun protection and light hydration. This is when you'll look and feel most radiant."
            ),
            ContentSection(
                heading = "Luteal Phase (Days 15-28)",
                body = "Rising progesterone increases oil production, potentially leading to breakouts. Switch to oil-free products, use salicylic acid or niacinamide to prevent acne, and don't skip cleansing. Clay masks can help control excess oil."
            )
        )
    ),
    ContentArticle(
        id = "meditation_mindfulness",
        title = "Meditation & Mindfulness for Women",
        subtitle = "Simple practices to support your emotional well-being",
        category = "wellness",
        readingTimeMinutes = 5,
        sections = listOf(
            ContentSection(
                heading = "Why Meditation Helps",
                body = "Research shows that regular meditation can reduce PMS symptoms, lower cortisol levels, improve sleep quality, and enhance emotional regulation. Even 5 minutes a day can make a meaningful difference in how you feel throughout your cycle."
            ),
            ContentSection(
                heading = "Breathing Exercises",
                body = "Try the 4-7-8 technique: inhale for 4 counts, hold for 7, exhale for 8. This activates your parasympathetic nervous system and can ease cramps, anxiety, and tension. Practice it before bed or during stressful moments."
            ),
            ContentSection(
                heading = "Body Scan Meditation",
                body = "Lie down comfortably and slowly bring attention to each part of your body, from toes to head. Notice any tension or discomfort without judgment. This practice is especially helpful during menstruation for connecting with and honoring your body."
            ),
            ContentSection(
                heading = "Gratitude Journaling",
                body = "Each evening, write down three things you're grateful for. This simple practice has been shown to improve mood, sleep, and overall well-being. During your luteal phase when mood may dip, gratitude journaling can be a powerful tool."
            ),
            ContentSection(
                heading = "Movement Meditation",
                body = "Not all meditation requires sitting still. Gentle yoga, walking in nature, or even stretching with intentional breathing counts as mindful movement. These practices are perfect for any cycle phase and help connect mind and body."
            )
        )
    ),
    ContentArticle(
        id = "nutrition_cycle",
        title = "Nutrition by Cycle Phase",
        subtitle = "Eat in sync with your hormones for better well-being",
        category = "wellness",
        readingTimeMinutes = 6,
        sections = listOf(
            ContentSection(
                heading = "Cycle Syncing Your Diet",
                body = "Just as your energy and mood shift throughout your cycle, your nutritional needs change too. Eating in alignment with your hormonal phases can help reduce PMS, boost energy, and support overall health."
            ),
            ContentSection(
                heading = "Menstruation: Replenish",
                body = "Focus on iron-rich foods (spinach, lentils, red meat, dark chocolate), anti-inflammatory foods (turmeric, ginger, fatty fish), and warming foods (soups, stews). Magnesium-rich foods like nuts and seeds help ease cramps."
            ),
            ContentSection(
                heading = "Follicular: Energize",
                body = "Support rising energy with light, fresh foods: salads, fermented foods (kimchi, yogurt), lean proteins, and sprouted grains. This phase favors lighter meals that support your body's increasing activity level."
            ),
            ContentSection(
                heading = "Ovulation: Fuel",
                body = "Your metabolism may slightly increase. Focus on fiber-rich vegetables, whole grains, and antioxidant-rich fruits like berries. Raw foods and smoothies are especially satisfying during this high-energy phase."
            ),
            ContentSection(
                heading = "Luteal: Comfort & Balance",
                body = "Cravings are real and valid! Complex carbohydrates (sweet potatoes, brown rice, oats) help boost serotonin. B-vitamins from leafy greens support mood. Dark chocolate satisfies sweet cravings while providing magnesium. Don't restrict \u2014 nourish."
            )
        )
    )
)
