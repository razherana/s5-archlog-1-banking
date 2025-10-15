#!/bin/bash

# Calcul du tableau d'amortissement
calculate_amortization_table() {
    local capital=$1
    local annual_rate=$2
    local months=$3
    
    # Calcul du taux mensuel
    monthly_rate=$(echo "scale=10; $annual_rate / 12 / 100" | bc -l)
    
    # Calcul de la mensualité avec la formule
    monthly_payment=$(echo "scale=10; 
        rate=$monthly_rate; 
        capital=$capital; 
        months=$months;
        numerator=capital*rate;
        denominator=1-1/(1+rate)^months;
        numerator/denominator" | bc -l)
    
    echo "================================================"
    echo "TABLEAU D'AMORTISSEMENT"
    echo "================================================"
    echo "Capital: $capital MGA"
    echo "Taux annuel: $annual_rate%"
    echo "Taux mensuel: $(echo "scale=6; $monthly_rate * 100" | bc)%"
    echo "Durée: $months mois"
    echo "Mensualité: $(echo "scale=2; $monthly_payment / 1" | bc) MGA"
    echo "================================================"
    echo "Mois | Mensualité | Intérêts | Capital | Restant"
    echo "-----|------------|----------|---------|--------"
    
    current_capital=$capital
    total_interest=0
    total_paid=0
    
    for ((month=1; month<=months; month++)); do
        # Calcul des intérêts du mois
        interest=$(echo "scale=10; $current_capital * $monthly_rate" | bc -l)
        
        # Calcul du capital remboursé
        capital_paid=$(echo "scale=10; $monthly_payment - $interest" | bc -l)
        
        # Nouveau capital restant
        new_capital=$(echo "scale=10; $current_capital - $capital_paid" | bc -l)
        
        # Arrondi pour affichage
        interest_display=$(echo "scale=2; $interest / 1" | bc)
        capital_paid_display=$(echo "scale=2; $capital_paid / 1" | bc)
        new_capital_display=$(echo "scale=2; $new_capital / 1" | bc)
        monthly_payment_display=$(echo "scale=2; $monthly_payment / 1" | bc)
        
        printf "%4d | %9.2fMGA | %7.2fMGA | %6.2fMGA | %7.2fMGA\n" \
            "$month" "$monthly_payment_display" "$interest_display" \
            "$capital_paid_display" "$new_capital_display"
        
        # Mise à jour des totaux
        total_interest=$(echo "scale=10; $total_interest + $interest" | bc -l)
        total_paid=$(echo "scale=10; $total_paid + $monthly_payment" | bc -l)
        current_capital=$new_capital
    done
    
    echo "================================================"
    
    # Calcul des totaux
    total_interest_display=$(echo "scale=2; $total_interest / 1" | bc)
    total_paid_display=$(echo "scale=2; $total_paid / 1" | bc)
    
    echo "Total intérêts payés: $total_interest_display MGA"
    echo "Total remboursé: $total_paid_display MGA"
    echo "Vérification: Capital + Intérêts = $(echo "scale=2; $capital + $total_interest" | bc) MGA"
}

# Vérification avec votre exemple
echo "CALCUL DE VÉRIFICATION - PRÊT 6000MGA À 5.5% SUR 11 MOIS"
calculate_amortization_table 6000 5.5 12

echo ""
echo ""

