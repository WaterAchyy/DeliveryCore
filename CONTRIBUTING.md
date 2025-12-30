# Contributing to DeliveryCore

We welcome contributions to DeliveryCore! Here's how you can help:

## Getting Started

1. Fork the repository
2. Clone your fork: `git clone https://github.com/yourusername/DeliveryCore.git`
3. Create a new branch: `git checkout -b feature/your-feature-name`
4. Make your changes
5. Test your changes: `mvn test`
6. Commit your changes: `git commit -m "Add your feature"`
7. Push to your fork: `git push origin feature/your-feature-name`
8. Create a Pull Request

## Development Setup

### Requirements
- Java 8+ (recommended: Java 17+)
- Maven 3.6+
- Spigot/Paper 1.16.5+ test server

### Building
```bash
mvn clean package
```

### Testing
```bash
mvn test
```

## Code Style

- Use 4 spaces for indentation
- Follow Java naming conventions
- Add JavaDoc comments for public methods
- Keep methods under 50 lines when possible
- Write tests for new features

## Submitting Changes

1. Ensure all tests pass
2. Update documentation if needed
3. Add changelog entry
4. Create a clear pull request description

## Reporting Issues

When reporting bugs, please include:
- Minecraft version
- Server software (Spigot/Paper)
- Plugin version
- Steps to reproduce
- Error logs (if any)

## Feature Requests

We're open to new features! Please:
- Check existing issues first
- Describe the use case
- Explain why it would be useful
- Consider implementation complexity

## Questions?

Feel free to open an issue for questions or join our Discord server.

Thank you for contributing! 🎉