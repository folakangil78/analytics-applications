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

# graph_obj.display_graph()

def dfs_destinations(airport_graph, starting_node):
    already_visited = set()  # creates set to keep track of nodes already visited (set prevents duplicates)
    visiting_stack = [starting_node]  # initializes stack for depth-first with first node visited, LIFO (last in first visited) as per stack structure
    
    while visiting_stack:  # (is empty or has space)
        current_node = visiting_stack.pop() # removing top node from stack after processing (which was most recent visited and deepest in branch)
        
        if current_node not in already_visited: # only appending to visiting_stack if not already visited
            print(current_node) # print based on branch depth
            already_visited.add(current_node) # putting node into set to denote as visited
            
            for neighbor_connection in airport_graph[current_node]: # adding unvisited nodes to stack to be added for next while iteration
                if neighbor_connection not in already_visited:
                    visiting_stack.append(neighbor_connection) # adds to stack based on connected children in current branch

dfs_destinations(graph_obj.graph, 'ORD')