package com.example.data

data class ScientificItem(
    val char: String,
    val name: String,
    val usage: String,
    val latex: String,
    val category: String
)

object ScientificSymbols {
    val items = listOf(
        // --- MATHEMATICS SYMBOLS ---
        ScientificItem(
            char = "π",
            name = "Pi Constant",
            usage = "Ratio of circle's circumference to diameter (≈ 3.14159)",
            latex = "\\pi",
            category = "Mathematics"
        ),
        ScientificItem(
            char = "e",
            name = "Euler's Number",
            usage = "Base of natural logarithms, limit of (1 + 1/n)^n (≈ 2.71828)",
            latex = "e",
            category = "Mathematics"
        ),
        ScientificItem(
            char = "Σ",
            name = "Summation (Sigma)",
            usage = "Sum of a sequence or terms",
            latex = "\\sum",
            category = "Mathematics"
        ),
        ScientificItem(
            char = "∫",
            name = "Integral",
            usage = "Accumulation of quantities, area under curve",
            latex = "\\int",
            category = "Mathematics"
        ),
        ScientificItem(
            char = "√",
            name = "Square Root",
            usage = "Principal second root of a number",
            latex = "\\sqrt{}",
            category = "Mathematics"
        ),
        ScientificItem(
            char = "∞",
            name = "Infinity",
            usage = "Indicates a limit or boundless size",
            latex = "\\infty",
            category = "Mathematics"
        ),
        ScientificItem(
            char = "θ",
            name = "Theta (Angle)",
            usage = "Standard variable representation for plane angles",
            latex = "\\theta",
            category = "Mathematics"
        ),
        ScientificItem(
            char = "α",
            name = "Alpha",
            usage = "Used to denote alpha particles, angular acceleration, or coefficient",
            latex = "\\alpha",
            category = "Mathematics"
        ),
        ScientificItem(
            char = "β",
            name = "Beta",
            usage = "Used to denote beta particles, coefficient levels, or angles",
            latex = "\\beta",
            category = "Mathematics"
        ),
        ScientificItem(
            char = "Δ",
            name = "Delta (Difference/Change)",
            usage = "Represents change, increment, or discriminant",
            latex = "\\Delta",
            category = "Mathematics"
        ),
        ScientificItem(
            char = "∂",
            name = "Partial Derivative",
            usage = "Rates of change with respect to single variable in multivariable",
            latex = "\\partial",
            category = "Mathematics"
        ),
        ScientificItem(
            char = "∇",
            name = "Nabla (Gradient)",
            usage = "Vector differential gradient operator",
            latex = "\\nabla",
            category = "Mathematics"
        ),
        ScientificItem(
            char = "±",
            name = "Plus-Minus",
            usage = "Denotes approximation margins or two alternative options",
            latex = "\\pm",
            category = "Mathematics"
        ),
        ScientificItem(
            char = "≈",
            name = "Approximately Equal",
            usage = "Indicates high-degree numerical proximity",
            latex = "\\approx",
            category = "Mathematics"
        ),
        ScientificItem(
            char = "≠",
            name = "Not Equal",
            usage = "Indicates inequality of two expressions",
            latex = "\\neq",
            category = "Mathematics"
        ),

        // --- MATHEMATICS FORMULAS ---
        ScientificItem(
            char = "x = (-b ± √(b² - 4ac)) / 2a",
            name = "Quadratic Formula",
            usage = "Solves real or complex roots of equation ax² + bx + c = 0",
            latex = "x = \\frac{-b \\pm \\sqrt{b^2 - 4ac}}{2a}",
            category = "Mathematics"
        ),
        ScientificItem(
            char = "a² + b² = c²",
            name = "Pythagorean Theorem",
            usage = "Relates sides of a right-angled triangle where c is hypotenuse",
            latex = "a^2 + b^2 = c^2",
            category = "Mathematics"
        ),
        ScientificItem(
            char = "e^(iπ) + 1 = 0",
            name = "Euler's Identity",
            usage = "Connects 5 mathematical constants: e, i, pi, 1, and 0",
            latex = "e^{i\\pi} + 1 = 0",
            category = "Mathematics"
        ),
        ScientificItem(
            char = "A = πr²",
            name = "Area of Circle",
            usage = "Computes complete planar surface area using boundary radius r",
            latex = "A = \\pi r^2",
            category = "Mathematics"
        ),

        // --- PHYSICS FORMULAS ---
        ScientificItem(
            char = "E = mc²",
            name = "Mass-Energy Equivalence",
            usage = "Einstein's principle where mass translates directly to energy",
            latex = "E = mc^2",
            category = "Physics"
        ),
        ScientificItem(
            char = "F = ma",
            name = "Newton's Second Law",
            usage = "Force equals mass multiplied directly by acceleration rates",
            latex = "F = ma",
            category = "Physics"
        ),
        ScientificItem(
            char = "E = hν",
            name = "Planck's Relation",
            usage = "Relates photon energy directly to its wave frequency ν",
            latex = "E = h\\nu",
            category = "Physics"
        ),
        ScientificItem(
            char = "v = fλ",
            name = "Wave Equation",
            usage = "Computes uniform speed using frequency (f) times wavelength (λ)",
            latex = "v = f\\lambda",
            category = "Physics"
        ),
        ScientificItem(
            char = "PV = nRT",
            name = "Ideal Gas Law",
            usage = "Unifies pressure, volume, gas quantity, and ambient scale",
            latex = "PV = nRT",
            category = "Physics"
        ),
        ScientificItem(
            char = "F = G(m₁m₂) / r²",
            name = "Newton's Gravity Law",
            usage = "Attractive gravitational pull between dual masses separated by r",
            latex = "F = G \\frac{m_1 m_2}{r^2}",
            category = "Physics"
        ),
        ScientificItem(
            char = "F = k(q₁q₂) / r²",
            name = "Coulomb's Law",
            usage = "Electrostatic force acting between dual point charges",
            latex = "F = k_e \\frac{q_1 q_2}{r^2}",
            category = "Physics"
        ),

        // --- CHEMISTRY FORMULAS ---
        ScientificItem(
            char = "H₂O",
            name = "Water Molecule",
            usage = "Dihydrogen Monoxide (liquid catalyst compound of life)",
            latex = "H_2O",
            category = "Chemistry"
        ),
        ScientificItem(
            char = "CO₂",
            name = "Carbon Dioxide",
            usage = "Product of carbon decay, respiration and green greenhouse component",
            latex = "CO_2",
            category = "Chemistry"
        ),
        ScientificItem(
            char = "C₆H₁₂O₆",
            name = "Glucose Sugar",
            usage = "Hexose sugar monosaccharide used as cellular energy resource",
            latex = "C_6H_{12}O_6",
            category = "Chemistry"
        ),
        ScientificItem(
            char = "H₂SO₄",
            name = "Sulfuric Acid",
            usage = "Strong mineral acid with high concentration industrial use",
            latex = "H_2SO_4",
            category = "Chemistry"
        ),
        ScientificItem(
            char = "6CO₂ + 6H₂O → C₆H₁₂O₆ + 6O₂",
            name = "Photosynthesis Reaction",
            usage = "Process in plants converting carbon and water into glucose & oxygen",
            latex = "6CO_2 + 6H_2O \\rightarrow C_6H_{12}O_6 + 6O_2",
            category = "Chemistry"
        ),
        ScientificItem(
            char = "NaCl",
            name = "Sodium Chloride",
            usage = "Common edible table salt ionic compound",
            latex = "NaCl",
            category = "Chemistry"
        ),
        ScientificItem(
            char = "NaOH",
            name = "Sodium Hydroxide",
            usage = "Highly caustic alkaline basic solution used in soap cleaners",
            latex = "NaOH",
            category = "Chemistry"
        )
    )
}
