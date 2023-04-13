# Use the official Golang image as a base image
FROM golang:1.16-alpine AS build

# Set the working directory
WORKDIR /app

# Copy go.mod and go.sum files
COPY go.mod go.sum ./

# Download dependencies
RUN go mod download

# Copy the source code
COPY . .

# Build the application
RUN go build -o prometheus-alerts-handler .

# Use a lightweight Alpine image for the final image
FROM alpine:latest

# Install ca-certificates
RUN apk --no-cache add ca-certificates

# Copy the binary from the build stage
COPY --from=build /app/prometheus-alerts-handler /usr/local/bin/prometheus-alerts-handler

# Expose the port on which the application will run
EXPOSE 8080

# Run the application
CMD ["/usr/local/bin/prometheus-alerts-handler"]
