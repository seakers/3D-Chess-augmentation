import numpy as np

# Dummy autoencoder for demonstration
class Autoencoder:
    def __init__(self, pattern, latent_dim=10):
        self.pattern = pattern
        self.latent_dim = latent_dim

    def encode(self, x):
        # For demo: flatten and pad/truncate to latent_dim
        latent = np.ravel(x)
        if latent.shape[0] < self.latent_dim:
            latent = np.pad(latent, (0, self.latent_dim - latent.shape[0]), mode='constant')
        else:
            latent = latent[:self.latent_dim]
        return latent

    def decode(self, latent, original_shape):
        # For demo: reshape ignoring any extra padding
        decoded = latent[:np.prod(original_shape)].reshape(original_shape)
        return decoded

# Dictionary of autoencoders for our six decision patterns.
autoencoders = {
    'down_select': Autoencoder('down_select'),
    'partition':   Autoencoder('partition'),
    'assigning':   Autoencoder('assigning'),
    'permuting':   Autoencoder('permuting'),
    'combining':   Autoencoder('combining'),
    'connecting':  Autoencoder('connecting')
}

# A class representing a decision fragment.
class DecisionFragment:
    def __init__(self, pattern, encoding):
        self.pattern = pattern
        self.encoding = encoding
        self.original_shape = np.array(encoding).shape

    def to_latent(self):
        encoder = autoencoders[self.pattern]
        return encoder.encode(self.encoding)

    def from_latent(self, latent):
        encoder = autoencoders[self.pattern]
        self.encoding = encoder.decode(latent, self.original_shape).tolist()
        return self

# Latent space genetic operators (generic)
def latent_crossover(latent1, latent2, method="arithmetic"):
    if method == "arithmetic":
        alpha = np.random.rand(latent1.shape[0])
        return alpha * latent1 + (1 - alpha) * latent2
    elif method == "uniform":
        return np.array([np.random.choice([a, b]) for a, b in zip(latent1, latent2)])
    else:
        raise ValueError("Unsupported crossover method.")

def latent_mutation(latent, mutation_rate=0.1, noise_std=0.05):
    mutated = latent.copy()
    for i in range(len(mutated)):
        if np.random.rand() < mutation_rate:
            mutated[i] += np.random.normal(0, noise_std)
    return mutated

# --- Fragment-by-Fragment Approach ---
def evolve_fragment(fragment1, fragment2):
    # Encode to latent space
    latent1 = fragment1.to_latent()
    latent2 = fragment2.to_latent()
    
    # Apply crossover and mutation
    child_latent = latent_crossover(latent1, latent2, method="arithmetic")
    child_latent = latent_mutation(child_latent, mutation_rate=0.2, noise_std=0.1)
    
    # Decode back to native encoding
    child_fragment = DecisionFragment(fragment1.pattern, None)
    child_fragment.from_latent(child_latent)
    return child_fragment

# --- Whole-Solution Approach ---
def evolve_solution(fragments_parent1, fragments_parent2):
    # Assume fragments_parent1 and fragments_parent2 are lists of corresponding DecisionFragments.
    latent_vectors = []
    for frag1, frag2 in zip(fragments_parent1, fragments_parent2):
        # For each fragment, get its latent vector
        latent_vectors.append(frag1.to_latent())
    
    # Concatenate all latent vectors into one overall latent vector
    overall_latent1 = np.concatenate(latent_vectors)
    
    latent_vectors = []
    for frag1, frag2 in zip(fragments_parent1, fragments_parent2):
        latent_vectors.append(frag2.to_latent())
    overall_latent2 = np.concatenate(latent_vectors)
    
    # Apply crossover and mutation on the whole-solution latent vector
    overall_child_latent = latent_crossover(overall_latent1, overall_latent2, method="arithmetic")
    overall_child_latent = latent_mutation(overall_child_latent, mutation_rate=0.2, noise_std=0.1)
    
    # Decode back to individual fragments. Assume we know each fragment's latent_dim.
    child_fragments = []
    start = 0
    latent_dim = autoencoders['down_select'].latent_dim  # Assuming fixed dim for all
    for frag in fragments_parent1:
        end = start + latent_dim
        latent_part = overall_child_latent[start:end]
        child_frag = DecisionFragment(frag.pattern, None)
        child_frag.from_latent(latent_part)
        child_fragments.append(child_frag)
        start = end
    return child_fragments

# --- Demonstration ---
if __name__ == "__main__":
    np.random.seed(42)
    
    # Example: Two parent solutions for the down_select fragment.
    parent1_ds = DecisionFragment('down_select', [0, 1, 0, 1, 1, 0])
    parent2_ds = DecisionFragment('down_select', [1, 0, 1, 0, 1, 1])
    
    # Evolve on a fragment-by-fragment basis.
    child_ds = evolve_fragment(parent1_ds, parent2_ds)
    print("Child down_select (fragment-by-fragment):", child_ds.encoding)
    
    # Now assume a solution has three fragments (could be any combination of patterns).
    fragments_parent1 = [
        DecisionFragment('down_select', [0, 1, 0, 1, 1, 0]),
        DecisionFragment('partition',   [1, 1, 2, 1, 3, 2]),
        DecisionFragment('permuting',   [2, 3, 1, 4])
    ]
    fragments_parent2 = [
        DecisionFragment('down_select', [1, 0, 1, 0, 1, 1]),
        DecisionFragment('partition',   [2, 1, 2, 1, 3, 1]),
        DecisionFragment('permuting',   [3, 1, 4, 2])
    ]
    
    # Evolve as a whole solution.
    child_solution_fragments = evolve_solution(fragments_parent1, fragments_parent2)
    for frag in child_solution_fragments:
        print(f"Child {frag.pattern} fragment:", frag.encoding)
