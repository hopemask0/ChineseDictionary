package com.diploma.chinesedictionary.ui.screens.worddetail

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.diploma.chinesedictionary.domain.model.CharacterStrokes

/**
 * Composable с WebView для анимации иероглифа.
 *
 * Версия 4 (надёжная): hanzi-writer.min.js читается из assets через
 * AssetManager и встраивается inline в HTML. WebView'у не нужно ничего
 * "находить" — весь JS-код приходит вместе с HTML строкой.
 *
 * Преимущества:
 *   - Нет проблем с резолвом путей в WebView (раньше file:///android_asset/
 *     не находил файл)
 *   - Работает даже без Clean+Rebuild после добавления assets
 *   - Один файл, никаких внешних зависимостей в рантайме
 *
 * Если файла в assets нет — показываем понятную ошибку в WebView, а не
 * пустой экран.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HanziWriterView(
    strokes: CharacterStrokes,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    // Читаем библиотеку один раз при первом отображении.
    // remember с ключом context означает: пересчитываем только если контекст
    // сменился (а он не меняется в рамках одного экрана).
    val hanziWriterJs = remember(context) { readAssetSafely(context, "hanzi-writer.min.js") }

    val backgroundColor = MaterialTheme.colorScheme.surface
    val backgroundHex = String.format("#%06X", 0xFFFFFF and backgroundColor.toArgb())

    val strokeColor = MaterialTheme.colorScheme.onBackground
    val strokeHex = String.format("#%06X", 0xFFFFFF and strokeColor.toArgb())

    val accentColor = MaterialTheme.colorScheme.primary
    val accentHex = String.format("#%06X", 0xFFFFFF and accentColor.toArgb())

    val onAccentColor = MaterialTheme.colorScheme.onPrimary
    val onAccentHex = String.format("#%06X", 0xFFFFFF and onAccentColor.toArgb())

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(360.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface),
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    setBackgroundColor(Color.Transparent.toArgb())
                    webViewClient = WebViewClient()
                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                            Log.d(
                                "HanziWriter",
                                "${message.messageLevel()}: ${message.message()}",
                            )
                            return true
                        }
                    }
                }
            },
            update = { webView ->
                val html = buildHanziWriterHtml(
                    strokes = strokes,
                    hanziWriterJs = hanziWriterJs,
                    backgroundHex = backgroundHex,
                    strokeHex = strokeHex,
                    accentHex = accentHex,
                    onAccentHex = onAccentHex,
                )
                webView.loadDataWithBaseURL(
                    null,  // baseURL не нужен — всё inline
                    html,
                    "text/html",
                    "utf-8",
                    null,
                )
            },
        )
    }
}

/**
 * Безопасно читает файл из assets. Если файла нет — возвращает null,
 * и в HTML покажется понятное сообщение об ошибке.
 */
private fun readAssetSafely(context: Context, filename: String): String? {
    return try {
        context.assets.open(filename).bufferedReader().use { it.readText() }
    } catch (e: Exception) {
        Log.e("HanziWriter", "Не удалось прочитать $filename из assets: ${e.message}")
        null
    }
}

private fun buildHanziWriterHtml(
    strokes: CharacterStrokes,
    hanziWriterJs: String?,
    backgroundHex: String,
    strokeHex: String,
    accentHex: String,
    onAccentHex: String,
): String {
    // Если библиотеки нет в assets — показываем понятную ошибку
    if (hanziWriterJs == null) {
        return """
            <!DOCTYPE html>
            <html><body style="background: $backgroundHex; padding: 20px; font-family: sans-serif;">
                <p style="color: red;">Файл <b>hanzi-writer.min.js</b> не найден в папке assets/.</p>
                <p style="color: #666; font-size: 13px;">
                    Положите файл в <code>app/src/main/assets/hanzi-writer.min.js</code>
                    и сделайте Clean + Rebuild проекта.
                </p>
            </body></html>
        """.trimIndent()
    }

    val strokesJson = strokes.strokes.joinToString(",") { "\"${it.replace("\"", "\\\"")}\"" }
    val mediansJson = strokes.medians.joinToString(",") { stroke ->
        "[" + stroke.joinToString(",") { point ->
            "[${point.joinToString(",")}]"
        } + "]"
    }

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body {
                    margin: 0;
                    padding: 20px 0;
                    background: $backgroundHex;
                    font-family: -apple-system, sans-serif;
                    text-align: center;
                }
                #character-container {
                    width: 240px;
                    height: 240px;
                    margin: 0 auto;
                }
                #replay {
                    display: inline-block;
                    margin-top: 20px;
                    padding: 10px 24px;
                    border: none;
                    border-radius: 12px;
                    background: $accentHex;
                    color: $onAccentHex;
                    font-size: 14px;
                    font-weight: 500;
                    cursor: pointer;
                }
                #replay:active { opacity: 0.7; }
            </style>
        </head>
        <body>
            <div id="character-container"></div>
            <br>
            <button id="replay">Повторить ↻</button>

            <script>
                // Inline-встроенная библиотека hanzi-writer (~70KB)
                $hanziWriterJs
            </script>
            <script>
                const characterData = {
                    strokes: [$strokesJson],
                    medians: [$mediansJson]
                };

                const writer = HanziWriter.create('character-container', '${strokes.character}', {
                    width: 240,
                    height: 240,
                    padding: 5,
                    strokeColor: '$strokeHex',
                    radicalColor: '$accentHex',
                    delayBetweenStrokes: 200,
                    strokeAnimationSpeed: 1.2,
                    charDataLoader: function(char, onComplete) {
                        onComplete(characterData);
                    }
                });

                writer.animateCharacter();

                document.getElementById('replay').addEventListener('click', function() {
                    writer.animateCharacter();
                });
            </script>
        </body>
        </html>
    """.trimIndent()
}