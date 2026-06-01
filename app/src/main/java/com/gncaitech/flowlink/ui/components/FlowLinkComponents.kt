package com.gncaitech.flowlink.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gncaitech.flowlink.ui.theme.ArtRed
import com.gncaitech.flowlink.ui.theme.G100
import com.gncaitech.flowlink.ui.theme.G200
import com.gncaitech.flowlink.ui.theme.G500
import com.gncaitech.flowlink.ui.theme.G700
import com.gncaitech.flowlink.ui.theme.MedTeal
import com.gncaitech.flowlink.ui.theme.MontserratFamily
import com.gncaitech.flowlink.ui.theme.Navy
import com.gncaitech.flowlink.ui.theme.RedLight
import com.gncaitech.flowlink.ui.theme.TealLight

// ---------------------------------------------------------------------------
// FlTextField
// ---------------------------------------------------------------------------

@Composable
fun FlTextField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit = {},
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    focused: Boolean = false,
    supportingText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    trailingLabel: String? = null,
    isPassword: Boolean = false,
    onTrailingIconClick: (() -> Unit)? = null,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null,
    isError: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        readOnly = readOnly,
        label = { Text(label, fontSize = 13.sp) },
        isError = isError,
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else keyboardType),
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = if (focused) Navy else G500,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        trailingIcon = when {
            trailingLabel != null -> {
                {
                    Text(
                        trailingLabel,
                        style = TextStyle(
                            fontFamily = MontserratFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = G500
                        ),
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
            }
            trailingIcon != null -> {
                {
                    IconButton(onClick = { onTrailingIconClick?.invoke() }) {
                        Icon(
                            imageVector = trailingIcon,
                            contentDescription = null,
                            tint = Navy,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            else -> null
        },
        supportingText = supportingText?.let { { Text(it, fontSize = 11.sp, color = if (isError) ArtRed else G500) } },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Navy,
            unfocusedBorderColor = G200,
            focusedLabelColor = Navy,
            unfocusedLabelColor = G500,
            cursorColor = Navy,
        ),
        shape = RoundedCornerShape(12.dp),
    )
}

// ---------------------------------------------------------------------------
// FilledButton
// ---------------------------------------------------------------------------

@Composable
fun FilledButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    leadingIcon: ImageVector? = null,
    color: Color = ArtRed,
    height: androidx.compose.ui.unit.Dp = 48.dp,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(height),
        shape = RoundedCornerShape(100.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = PaddingValues(horizontal = 24.dp),
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier
                    .size(18.dp)
                    .padding(end = 0.dp)
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text,
            style = TextStyle(
                fontFamily = MontserratFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 0.02.sp,
                color = Color.White
            )
        )
    }
}

// ---------------------------------------------------------------------------
// OutlinedActionButton (named to avoid conflict with Material3 OutlinedButton)
// ---------------------------------------------------------------------------

@Composable
fun OutlinedActionButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    leadingIcon: ImageVector? = null,
    color: Color = Navy,
    height: androidx.compose.ui.unit.Dp = 48.dp,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(height),
        enabled = enabled,
        shape = RoundedCornerShape(100.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = color),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, color),
        contentPadding = PaddingValues(horizontal = 24.dp),
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text,
            style = TextStyle(
                fontFamily = MontserratFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 0.02.sp,
                color = color
            )
        )
    }
}

// ---------------------------------------------------------------------------
// AppBar
// ---------------------------------------------------------------------------

@Composable
fun AppBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Navy
                )
            }
        } else {
            Spacer(Modifier.width(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = TextStyle(
                    fontFamily = MontserratFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Navy
                )
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = TextStyle(
                        fontFamily = MontserratFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        letterSpacing = (0.16f * 10f).sp,
                        color = MedTeal
                    )
                )
            }
        }
        trailingContent?.invoke()
        Spacer(Modifier.width(8.dp))
    }
}

// ---------------------------------------------------------------------------
// SubjectCard
// ---------------------------------------------------------------------------

@Composable
fun SubjectCard(
    name: String,
    pid: String,
    age: Int,
    gender: String,
    surgery: String,
    program: String,
    scheduled: String,
    status: String,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val bgColor = if (selected) TealLight else Color.White
    val borderColor = if (selected) MedTeal else G200
    val borderWidth = if (selected) 2.dp else 1.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Radio indicator
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(if (selected) MedTeal else Color.Transparent)
                        .border(2.dp, if (selected) MedTeal else G200, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                // Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (selected) MedTeal.copy(alpha = 0.15f) else G100),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        name.take(1),
                        style = TextStyle(
                            fontFamily = MontserratFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (selected) MedTeal else G700
                        )
                    )
                }

                Spacer(Modifier.width(12.dp))

                // Info column
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            name,
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = G700
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        // Status badge
                        val (badgeText, badgeBg, badgeFg) = if (status == "watch") {
                            Triple("관찰필요", RedLight, ArtRed)
                        } else {
                            Triple("대기", TealLight, MedTeal)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(badgeBg)
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                badgeText,
                                style = TextStyle(
                                    fontFamily = MontserratFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp,
                                    color = badgeFg
                                )
                            )
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "$pid · ${age}세 $gender · $surgery",
                        style = TextStyle(fontSize = 11.sp, color = G500)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "예약 $scheduled",
                        style = TextStyle(fontSize = 11.sp, color = G500)
                    )
                }
            }

//            Spacer(Modifier.height(12.dp))
//
//            Box(
//                modifier = Modifier
//                    .clip(RoundedCornerShape(8.dp))
//                    .background(G100)
//                    .padding(horizontal = 8.dp, vertical = 4.dp)
//            ) {
//                Text(
//                    program,
//                    style = TextStyle(fontSize = 10.sp, color = G700, fontWeight = FontWeight.Medium)
//                )
//            }
        }
    }
}

// ---------------------------------------------------------------------------
// FormCard
// ---------------------------------------------------------------------------

@Composable
fun FormCard(
    caption: String,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, G200, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        FLCaption(caption)
        Spacer(Modifier.height(4.dp))
        Text(
            title,
            style = TextStyle(
                fontFamily = MontserratFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Navy
            )
        )
        Spacer(Modifier.height(16.dp))
        content()
    }
}

// ---------------------------------------------------------------------------
// RightActionBtn
// ---------------------------------------------------------------------------

@Composable
fun RightActionBtn(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            style = TextStyle(
                fontFamily = MontserratFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 9.sp,
                letterSpacing = 0.02.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        )
    }
}

// ---------------------------------------------------------------------------
// MetricChip
// ---------------------------------------------------------------------------

@Composable
fun MetricChip(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, MedTeal.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Teal glow dot
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MedTeal)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                label,
                style = TextStyle(
                    fontFamily = MontserratFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 9.sp,
                    letterSpacing = 0.14.sp,
                    color = MedTeal
                )
            )
        }
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                value,
                style = TextStyle(
                    fontFamily = MontserratFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 26.sp,
                    color = Color.White
                )
            )
            Spacer(Modifier.width(2.dp))
            Text(
                unit,
                style = TextStyle(
                    fontFamily = MontserratFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                ),
                modifier = Modifier.padding(bottom = 3.dp)
            )
        }
    }
}
