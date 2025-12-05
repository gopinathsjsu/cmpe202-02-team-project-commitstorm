# CampusMarket - Student Marketplace Landing Page

A modern, responsive React landing page for Commit Storm Market - a student-only marketplace platform.

## Features

- **Modern Dark Theme**: Clean, professional dark design with gray-900 background
- **Responsive Design**: Fully responsive layout that works on all devices
- **Interactive Elements**: Functional search bar with state management
- **Sticky Navigation**: Navigation bar that stays at the top when scrolling
- **Feature Cards**: Three highlighted features with hover effects
- **Tailwind CSS**: Utility-first CSS framework for rapid styling

## Technology Stack

- **React 18**: Modern React with hooks
- **Vite**: Fast build tool and development server
- **Tailwind CSS**: Utility-first CSS framework
- **JavaScript**: ES6+ features

## Getting Started

1. Install dependencies:
   ```bash
   npm install
   ```

2. Start the development server:
   ```bash
   npm run dev
   ```

3. Open your browser and navigate to the local development URL (usually `http://localhost:5173`)

## Project Structure

```
src/
├── App.jsx          # Main landing page component
├── App.css          # App-specific styles (minimal)
├── index.css        # Tailwind CSS imports
└── main.jsx         # React app entry point
```

## Components

The landing page includes:

- **Navbar**: Sticky navigation with CampusMarket branding and auth buttons
- **Hero Section**: Main headline, sub-headline, and search functionality
- **Features Section**: Three feature cards explaining how the platform works
- **Footer**: Simple copyright footer

## Responsive Breakpoints

- Mobile: `< 768px`
- Tablet: `768px - 1024px`
- Desktop: `> 1024px`

All components are designed to be fully responsive using Tailwind CSS responsive utilities.