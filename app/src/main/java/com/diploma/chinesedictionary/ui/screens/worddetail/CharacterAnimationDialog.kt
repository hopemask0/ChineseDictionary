package com.diploma.chinesedictionary.ui.screens.worddetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.diploma.chinesedictionary.domain.model.CharacterStrokes
import com.diploma.chinesedictionary.domain.repository.DictionaryRepository
import com.diploma.chinesedictionary.ui.theme.InkMuted
import org.koin.compose.koinInject

/**
 * Полноэкранный диалог с анимацией написания иероглифа.
 *
 * Загружает данные штрихов с сервера при открытии. Пока грузится — показывает
 * прогресс. После загрузки — отдаёт данные в HanziWriterView, который через
 * WebView рисует анимацию.
 *
 * Использование (внутри composable):
 *   if (showAnimation) {
 *     CharacterAnimationDialog(
 *       character = "学",
 *       onDismiss = { showAnimation = false }
 *     )
 *   }
 *
 * koinInject() — это аналог koinViewModel(), но для обычных не-ViewModel
 * зависимостей. Используется когда мы хотим получить репозиторий или сервис
 * напрямую в composable без ViewModel-обёртки. Подходит для простых случаев
 * вроде нашего — один разовый запрос за данными.
 */
@Composable
fun CharacterAnimationDialog(
    character: String,
    onDismiss: () -> Unit,
) {
    val repository: DictionaryRepository = koinInject()

    // Состояние загрузки — локальное в этом composable. Так как диалог
    // используется недолго, отдельный ViewModel избыточен.
    var strokes by remember { mutableStateOf<CharacterStrokes?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // LaunchedEffect(character) — запустится при первом отображении диалога
    // и при изменении character. Внутри блока — корутина, которая отменится
    // если диалог закроется.
    LaunchedEffect(character) {
        isLoading = true
        error = null
        repository.getCharacterStrokes(character)
            .onSuccess { strokes = it; isLoading = false }
            .onFailure { error = "Не удалось загрузить данные"; isLoading = false }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,  // позволяем диалогу быть шире
        ),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Шапка с заголовком и кнопкой закрыть
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.align(Alignment.CenterStart)) {
                        Text(
                            text = "笔画",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "ПОРЯДОК НАПИСАНИЯ · $character",
                            style = MaterialTheme.typography.labelSmall,
                            color = InkMuted,
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterEnd),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Закрыть",
                            tint = InkMuted,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Контент: либо загрузка, либо ошибка, либо анимация
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(360.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    error != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(360.dp)
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = error ?: "",
                                color = InkMuted,
                            )
                        }
                    }
                    strokes != null -> {
                        HanziWriterView(strokes = strokes!!)
                    }
                }
            }
        }
    }
}