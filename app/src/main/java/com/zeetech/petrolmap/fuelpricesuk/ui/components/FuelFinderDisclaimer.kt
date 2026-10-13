package com.zeetech.petrolmap.fuelpricesuk.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolBrandGreen
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolMutedText

private const val FUEL_FINDER_URL = "https://www.gov.uk/government/collections/fuel-finder"

@Composable
fun FuelFinderDisclaimer(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 11.sp,
    textAlign: TextAlign = TextAlign.Center
) {
    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(color = PetrolMutedText)) {
            append("Disclaimer: Prices sourced via ")
        }
        withLink(
            LinkAnnotation.Url(
                url = FUEL_FINDER_URL,
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = PetrolBrandGreen,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.Underline
                    )
                )
            )
        ) {
            append("Fuel Finder")
        }
        withStyle(style = SpanStyle(color = PetrolMutedText)) {
            append(" & CMA Open Data Scheme. Always verify at the pump.")
        }
    }

    Text(
        text = annotatedString,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        fontSize = fontSize,
        textAlign = textAlign,
        lineHeight = 15.sp
    )
}
