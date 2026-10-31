# MT5 Trade Calculator

A simple offline Android calculator for manually preparing trades in MetaTrader 5. It does not connect to MT5, request account credentials, or place trades.

## Features
- BUY/SELL calculations
- Fixed 1:3 risk-to-reward ratio (TP points = SL points × 3)
- Position sizing from account balance and risk percentage
- Broker-specific symbol inputs: point size, tick size, tick value, and volume step
- Lot size rounded down to the broker's volume step

## Position sizing
Risk amount = balance × risk% ÷ 100

Loss per 1.0 lot = (SL points × point size ÷ tick size) × tick value

Raw lots = risk amount ÷ loss per 1.0 lot

The final lot size is rounded DOWN to the broker's volume step. Verify all symbol specifications in your broker's MT5 platform before using the result. Tick value can vary by symbol and account currency.

## Build
Open this project in Android Studio and build the `app` module, or run:

```sh
gradle assembleDebug
```

Minimum Android version: Android 6.0 (API 23).

## Disclaimer
For calculation assistance only, not financial advice. Trading involves risk. Check all values and broker constraints before placing any trade.
