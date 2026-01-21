import networkx as nx
import matplotlib.pyplot as plt
from pyvis.network import Network
import numpy as np

print("graph.py loaded successfully\n\n\n") # verification of importing graph module into main

def createGraph(corr_matrix, monthly_or_yearly_specifying_str):
    G = nx.Graph()

    # Add nodes with sizes based on total correlation magn
    node_sizes = {}
    for stock in corr_matrix.columns:
        total_correlation = np.sum(np.abs(corr_matrix[stock]))
        node_sizes[stock] = total_correlation * 50  # Scale factor for node sizes
        G.add_node(stock, size=node_sizes[stock])
        # tried to calculate node size by summing abs. value of correlations connecting to that stock
        # goal was to imply the size of stock as how "connected" the stock is to the rest

    # Add edges with weights and colors based on correlation
    for i, stock1 in enumerate(corr_matrix.columns):
        for j, stock2 in enumerate(corr_matrix.columns):
            if i < j:  # Avoid duplicates (symmetric matrix) --> (stock1,stock2) same as (stock2,stock1)
                corr = corr_matrix.loc[stock1, stock2]
                if corr != 0:  # Skip zero correlations
                    if corr > 0:
                        color = 'red'
                    else:
                        color = 'blue'
                    G.add_edge(stock1, stock2, weight=np.abs(corr), color=color)

    # matplotlib implementation of G:::
    edges = G.edges(data=True)
    edge_colors = [edge[2]['color'] for edge in edges]
    edge_weights = [edge[2]['weight'] for edge in edges]
    # Get node sizes for visualization
    node_sizes_list = [data['size'] for _, data in G.nodes(data=True)]

    plt.figure(figsize=(15, 15))

    # Spread out the nodes by adjusting the `k` parameter
    pos = nx.spring_layout(G, seed=42, k=25.5)  # increase `k` arg for more space between nodes

    # Draw graph
    nx.draw(
        G, pos,
        with_labels=True,
        node_size=node_sizes_list,
        node_color="lightblue",
        edge_color=edge_colors,
        width=edge_weights,
        font_size=8,
        font_weight="bold",
    )

    # Add edge labels for correlation values
    edge_labels = {(stock1, stock2): f'{np.round(corr_matrix.loc[stock1, stock2], 2)}' # dict where keys are stock pairs and values are correlations
                   for i, stock1 in enumerate(corr_matrix.columns) # loc function from numpy accesses corr value and formats as str
                   for j, stock2 in enumerate(corr_matrix.columns) # enumerate fxn returns given index (i) and column name (which will be a given stock) for each part of tuple
                   if i < j and corr_matrix.loc[stock1, stock2] != 0} 
                # specifies i < j condition to ensure tuple pair only considered once, for ex i=0;j=1 counts but i=1;j=0 (which corresponds to same tuple) won't

    nx.draw_networkx_edge_labels(
        G, pos, edge_labels=edge_labels, font_size=6, font_color='black', font_weight="bold"
    )

    title_str = f'Stock Correlation Network ~ {monthly_or_yearly_specifying_str}'
    plt.title(title_str, fontsize=16)
    plt.show()