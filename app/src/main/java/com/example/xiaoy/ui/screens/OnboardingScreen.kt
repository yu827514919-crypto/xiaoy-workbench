package com.example.xiaoy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.xiaoy.data.AppState
import com.example.xiaoy.data.ChildProfile
import com.example.xiaoy.data.ImageRef
import com.example.xiaoy.ui.AppConfig
import com.example.xiaoy.ui.components.AppImage
import com.example.xiaoy.ui.theme.Apricot
import com.example.xiaoy.ui.theme.Cream
import com.example.xiaoy.ui.theme.Ink
import com.example.xiaoy.ui.theme.InkSoft
import com.example.xiaoy.ui.theme.PaperWarm

@Composable
fun OnboardingScreen(appState: AppState, onDone: () -> Unit) {
    var parent by remember { mutableStateOf("") }
    var child by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().background(Cream).verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.size(56.dp))
        AppImage(ImageRef.of("img_logo"), Modifier.size(84.dp).clip(RoundedCornerShape(22.dp)))
        Spacer(Modifier.size(16.dp))
        Text("小芽", style = MaterialTheme.typography.headlineMedium, color = Ink, fontWeight = FontWeight.Bold)
        Spacer(Modifier.size(6.dp))
        Text(AppConfig.SLOGAN, style = MaterialTheme.typography.bodyMedium, color = InkSoft)
        Spacer(Modifier.size(32.dp))

        Text("先认识一下你和孩子", style = MaterialTheme.typography.titleMedium, color = Ink,
            fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.size(6.dp))
        Text("这些信息只保存在你的手机上，用于让工作台更贴心。",
            style = MaterialTheme.typography.bodySmall, color = InkSoft, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.size(20.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OnboardField("怎么称呼你（如：悦悦妈）", parent) { parent = it }
            OnboardField("孩子的昵称（如：小满）", child) { child = it }
            OnboardField("孩子出生日期（如 2020-03-12）", birthday) { birthday = it }
            OnboardField("所在城市（如：杭州）", city) { city = it }
        }

        Spacer(Modifier.size(32.dp))
        Button(
            onClick = {
                appState.saveProfile(
                    ChildProfile(
                        parentName = parent.trim(), childName = child.trim(),
                        childBirthday = birthday.trim(), city = city.trim(),
                        motto = AppConfig.SLOGAN
                    )
                )
                onDone()
            },
            enabled = child.isNotBlank() && parent.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = Apricot, disabledContainerColor = Apricot.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(50.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("开始记录", style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.size(12.dp))
        Text("打开即进入已有的示例工作台，随时可修改或删除",
            style = MaterialTheme.typography.labelSmall, color = InkSoft, textAlign = TextAlign.Center)
        Spacer(Modifier.size(40.dp))
    }
}

@Composable
private fun OnboardField(label: String, value: String, onChange: (String) -> Unit) {
    TextField(
        value = value, onValueChange = onChange,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = PaperWarm, unfocusedContainerColor = PaperWarm,
            focusedIndicatorColor = Apricot, unfocusedIndicatorColor = Color.Transparent
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
