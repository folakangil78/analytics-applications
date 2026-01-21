from collections import defaultdict

class Graph:
    def __init__(self):
        self.graph = defaultdict(list)

    def addEdge(self, u, v): # u is vertex 1 and v is vertex 2, insert as quotations for each airport
        self.graph[u].append(v)
        self.graph[v].append(u) # graph bidirectionality
        # function to create edge (flight) between two airports

    def display_graph(self):
        for airport_dest in self.graph: # accesses graph field of Graph class
            print(f"{airport_dest}: {self.graph[airport_dest]}") # printing out adjacency list for graph
    
graph_obj = Graph() # basically creating object instance of class above

# could have done with iteration through list as well for edge creation bw airports
graph_obj.addEdge('ORD', 'CVG')
graph_obj.addEdge('JFK', 'ORD')
graph_obj.addEdge('ORD', 'MSP')
graph_obj.addEdge('SFO', 'SAN')
graph_obj.addEdge('SAN', 'PHX')
graph_obj.addEdge('PHX', 'DEN')
graph_obj.addEdge('IAH', 'ATL')
graph_obj.addEdge('ATL', 'JFK')
graph_obj.addEdge('DEN', 'IAH')
graph_obj.addEdge('ORD', 'DEN')
graph_obj.addEdge('SEA', 'DEN')
graph_obj.addEdge('MSP', 'DEN')
graph_obj.addEdge('CVG', 'JFK')
graph_obj.addEdge('SEA', 'SFO')

graph_obj.display_graph()