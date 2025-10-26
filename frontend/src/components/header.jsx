import { useState } from 'react';

const Header = ({ user, onLoginClick, onSignupClick, onLogout }) => {
  const [searchQuery, setSearchQuery] = useState('')

  return (
    <div>
      <nav className="sticky top-0 bg-gray-900 border-b border-gray-800 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            {/* Logo */}
            <div className="flex-shrink-0">
              <h1 className="text-2xl font-bold text-white">Commit Storm Market</h1>
            </div>
            
            {/* Navigation Links */}
            <div className="hidden md:block">
              <div className="flex items-center space-x-8">
                <a href="#" className="text-gray-300 hover:text-white px-3 py-2 text-sm font-medium transition-colors">
                  Browse
                </a>
                <a href="#" className="text-gray-300 hover:text-white px-3 py-2 text-sm font-medium transition-colors">
                  Sell
                </a>
                <a href="#" className="text-gray-300 hover:text-white px-3 py-2 text-sm font-medium transition-colors">
                  About
                </a>
              </div>
            </div>
            
            {/* Auth Buttons */}
            <div className="flex items-center space-x-4">
              {user ? (
                // Show user info and logout when authenticated
                <div className="flex items-center space-x-4">
                  <span className="text-gray-300 text-sm">
                    Welcome, {user.name}!
                  </span>
                  <button 
                    onClick={onLogout}
                    className="border border-gray-300 text-gray-300 hover:text-white hover:border-white px-4 py-2 rounded-md text-sm font-medium transition-colors"
                  >
                    Logout
                  </button>
                </div>
              ) : (
                // Show login/signup buttons when not authenticated
                <>
                  <button 
                    onClick={onLoginClick}
                    className="border border-gray-300 text-gray-300 hover:text-white hover:border-white px-4 py-2 rounded-md text-sm font-medium transition-colors"
                  >
                    Log In
                  </button>
                  <button 
                    onClick={onSignupClick}
                    className="bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors"
                  >
                    Sign Up
                  </button>
                </>
              )}
            </div>
          </div>
        </div>
      </nav>
      <section className="py-20 px-4 sm:px-6 lg:px-8">
        <div className="max-w-7xl mx-auto text-center">
          
          {/* Search Bar */}
          <div className="max-w-2xl mx-auto">
            <div className="flex flex-col sm:flex-row gap-4">
              <input
                type="text"
                placeholder="Search for a textbook..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="flex-1 px-4 py-3 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
              />
              <button className="bg-indigo-600 hover:bg-indigo-700 text-white px-8 py-3 rounded-lg font-medium transition-colors">
                Search
              </button>
            </div>
          </div>
        </div>
      </section>
    </div>
  )
};

export default Header;