import yfinance as yf
import pandas as pd
import networkx as nx
import matplotlib.pyplot as plt
import seaborn as sns
import numpy as np

# file module imports for graph script
from graph import createGraph


# Define stock tickers for major sectors
stocks_by_sector = {
    "Technology": ["AAPL", "MSFT", "GOOGL", "NVDA", "META", "ADBE", "ORCL", "INTC", "CSCO", "AMD"],
    "Healthcare": ["JNJ", "PFE", "MRK", "ABBV", "TMO", "AMGN", "GILD", "BIIB", "VRTX", "CVS"],
    "Automotive": ["TSLA", "F", "GM", "HMC", "TM", "RACE", "LCID", "NIO", "XPEV", "LI"],
    "Financials": ["JPM", "BAC", "C", "GS", "MS", "WFC", "BLK", "SCHW", "TFC", "AXP"],
    "Consumer Discretionary": ["AMZN", "HD", "MCD", "NKE", "SBUX", "DIS", "LOW", "CMG", "BKNG", "TGT"]
}

tech_stocks = {
    "Technology": ["AAPL", "MSFT", "GOOGL", "NVDA", "META", "ADBE", "ORCL", "INTC", "CSCO", "AMD"]
}
healthcare_stocks = {
    "Healthcare": ["JNJ", "PFE", "MRK", "ABBV", "TMO", "AMGN", "GILD", "BIIB", "VRTX", "CVS"]
}
auto_stocks = {
    "Automotive": ["TSLA", "F", "GM", "HMC", "TM", "RACE", "LCID", "NIO", "XPEV", "LI"]
}
finance_stocks = {
    "Financials": ["JPM", "BAC", "C", "GS", "MS", "WFC", "BLK", "SCHW", "TFC", "AXP"]
}
discretion_stocks = {
    "Consumer Discretionary": ["AMZN", "HD", "MCD", "NKE", "SBUX", "DIS", "LOW", "CMG", "BKNG", "TGT"]
}

stock_index = str(input("Enter the index of stocks you would like to see correlations for. " +
                        "You can choose from 'tech_stocks', 'healthcare_stocks', 'auto_stocks', 'finance_stocks', 'discretion_stocks', or a combination of all of them: stocks_by_sector\n\n" +
                        "stocks_by_sector recommended for viewing meaningful correlations.\n"))
if stock_index == 'stocks_by_sector':
    stock_index = stocks_by_sector
if stock_index == 'tech_stocks':
    stock_index = tech_stocks
if stock_index == 'healthcare_stocks':
    stock_index = healthcare_stocks
if stock_index == 'auto_stocks':
    stock_index = auto_stocks
if stock_index == 'finance_stocks':
    stock_index = finance_stocks
if stock_index == 'discretion_stocks':
    stock_index = discretion_stocks

# Rendering historical stock data
all_tickers = [ticker for sector in stock_index.values() for ticker in sector]
data = yf.download(all_tickers, start="2015-01-01", end="2024-01-01")['Adj Close']
# Accesses data for each stock ticker, given longer timeframe for more meaningful yearly data

# Resample to monthly closing prices (last trading day of month)
monthly_prices = data.resample('M').last()

# Calculate monthly returns ~ pandas percent change
monthly_returns = monthly_prices.pct_change().dropna()

# Resample to yearly closing prices
yearly_prices = data.resample('Y').last()

# Calculate yearly returns
# yearly_returns = yearly_prices.pct_change().dropna() implementation
yearly_returns = np.log(yearly_prices / yearly_prices.shift(1)).dropna() #alt scale for return calculation to line above

# Pandas correlation matrix for monthly returns
monthly_correlation_matrix = monthly_returns.corr()

# Pandas correlation matrix for yearly returns
yearly_correlation_matrix = yearly_returns.corr()

if monthly_correlation_matrix.empty or monthly_correlation_matrix.isna().all().all():
    # check to ensure matrices aren't empty resulting in blank heatmaps...
    print("Monthly correlation matrix empty or Nan")
else:
    # Vis monthly returns correlation matrix
    plt.figure(figsize=(12, 10))
    sns.heatmap(monthly_correlation_matrix, annot=False, cmap="coolwarm", vmin=-1, vmax=1,
                xticklabels=monthly_correlation_matrix.columns, yticklabels=monthly_correlation_matrix.columns)
    plt.title("Stock Return Correlation Matrix ~ Monthly")
    plt.show()

if yearly_correlation_matrix.empty or yearly_correlation_matrix.isna().all().all():
    print("Yearly correlation matrix empty or Nan")
else:
    # Visualize yearly returns correlation matrix
    plt.figure(figsize=(12, 10))
    sns.heatmap(yearly_correlation_matrix, annot=False, cmap="coolwarm", vmin=-1, vmax=1,
                xticklabels=yearly_correlation_matrix.columns, yticklabels=yearly_correlation_matrix.columns)
    plt.title("Stock Return Correlation Matrix ~ Yearly")
    plt.show()

#############################################################################

# Create graph ~ separate script

createGraph(monthly_correlation_matrix, 'Monthly') # will be first graph output
createGraph(yearly_correlation_matrix, 'Yearly') # will be second graph output

#############################################################################


