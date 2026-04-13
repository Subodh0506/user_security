# Use the official Redis image as the base
FROM redis:latest

# Create a directory for the volume (Redis uses /data by default)
WORKDIR /data

# Define the volume. This tells Docker that data in this directory 
# should be persisted and managed outside the container's filesystem.
VOLUME /data

# Expose the standard Redis port
EXPOSE 6379

# Start Redis with "appendonly yes" to ensure data is actually saved to disk
CMD ["redis-server", "--appendonly", "yes"]