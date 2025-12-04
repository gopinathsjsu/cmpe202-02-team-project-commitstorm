import { useState, useRef, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router';

const Header = ({ user, isHome, onLoginClick, onSignupClick, onLogout, onMyProfileClick, onMyListingsClick, onMyMessagesClick, onReportsClick }) => {
  const [searchQuery, setSearchQuery] = useState('')
  const [showProfileDropdown, setShowProfileDropdown] = useState(false)
  const navigate = useNavigate();
  const location = useLocation();
  const dropdownRef = useRef(null);

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setShowProfileDropdown(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  // Get search query from URL if on marketplace
  useEffect(() => {
    if (location.pathname === '/marketplace') {
      const params = new URLSearchParams(location.search);
      const searchParam = params.get('search') || '';
      setSearchQuery(searchParam);
    }
  }, [location]);

  const handleSearch = (e) => {
    e.preventDefault();
    if (location.pathname === '/marketplace') {
      // Update URL with search query
      const params = new URLSearchParams();
      if (searchQuery.trim()) {
        params.set('search', searchQuery.trim());
      }
      navigate(`/marketplace?${params.toString()}`);
    } else {
      // Navigate to marketplace with search query
      const params = new URLSearchParams();
      if (searchQuery.trim()) {
        params.set('search', searchQuery.trim());
      }
      navigate(`/marketplace?${params.toString()}`);
    }
  };

  return (
    <div>
      <nav className="sticky top-0 bg-gray-900 border-b border-gray-800 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            {/* Logo */}
            <div className="flex-shrink-0">
              <button
                onClick={() => navigate('/marketplace')}
                className="text-2xl font-bold text-white hover:text-indigo-400 transition-colors cursor-pointer"
              >
                Commit Storm Market
              </button>
            </div>
            
            {/* Navigation Links */}
            <div className="hidden md:block">
              <div className="flex items-center space-x-8">
                <a href="#" className="text-gray-300 hover:text-white px-3 py-2 text-sm font-medium transition-colors">
                  Browse
                </a>
                <a href="/create-listing" className="text-gray-300 hover:text-white px-3 py-2 text-sm font-medium transition-colors">
                  Sell
                </a>
                <a href="#" className="text-gray-300 hover:text-white px-3 py-2 text-sm font-medium transition-colors">
                  About
                </a>
              </div>
            </div>
            
            {/* Profile Dropdown */}
            <div className="flex items-center space-x-4 relative" ref={dropdownRef}>
              <button
                onClick={() => setShowProfileDropdown(!showProfileDropdown)}
                className="border border-gray-300 text-gray-300 hover:text-white hover:border-white px-4 py-2 rounded-md text-sm font-medium transition-colors flex items-center space-x-2"
              >
                <span>Profile</span>
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                </svg>
              </button>
              
              {showProfileDropdown && (
                <div className="absolute right-0 top-full mt-2 w-56 bg-gray-800 rounded-lg shadow-2xl py-2 z-50 border border-gray-700 overflow-hidden">
                  {user ? (
                    <>
                      <button
                        onClick={() => {
                          setShowProfileDropdown(false);
                          if (onMyProfileClick) onMyProfileClick();
                        }}
                        className="block w-full text-left px-4 py-2.5 text-sm text-gray-300 hover:bg-gray-700 hover:text-white transition-colors"
                      >
                        My Profile
                      </button>
                      <button
                        onClick={() => {
                          setShowProfileDropdown(false);
                          if (onMyListingsClick) onMyListingsClick();
                        }}
                        className="block w-full text-left px-4 py-2.5 text-sm text-gray-300 hover:bg-gray-700 hover:text-white transition-colors"
                      >
                        My Listings
                      </button>
                      <button
                        onClick={() => {
                          setShowProfileDropdown(false);
                          if (onMyMessagesClick) onMyMessagesClick();
                        }}
                        className="block w-full text-left px-4 py-2.5 text-sm text-gray-300 hover:bg-gray-700 hover:text-white transition-colors"
                      >
                        View My Messages
                      </button>
                      <div className="border-t border-gray-700 my-1"></div>
                      {user.role =='ADMIN' ? (
                        <>
                          <button
                          onClick={() => {
                            setShowProfileDropdown(false);
                            if (onReportsClick) onReportsClick();
                          }}
                          className="block w-full text-left px-4 py-2.5 text-sm text-gray-300 hover:bg-gray-700 hover:text-white transition-colors"
                          >
                          View Reports
                          </button>
                          <div className="border-t border-gray-700 my-1"></div>
                        </>
                      ): <></>}
                      <button
                        onClick={() => {
                          setShowProfileDropdown(false);
                          onLogout();
                        }}
                        className="block w-full text-left px-4 py-2.5 text-sm text-red-400 hover:bg-gray-700 hover:text-red-300 transition-colors"
                      >
                        Logout
                      </button>
                    </>
                  ) : (
                    <>
                      <button
                        onClick={() => {
                          setShowProfileDropdown(false);
                          onLoginClick();
                        }}
                        className="block w-full text-left px-4 py-2 text-sm text-gray-300 hover:bg-gray-700 hover:text-white transition-colors"
                      >
                        Log In
                      </button>
                      <button
                        onClick={() => {
                          setShowProfileDropdown(false);
                          onSignupClick();
                        }}
                        className="block w-full text-left px-4 py-2 text-sm text-gray-300 hover:bg-gray-700 hover:text-white transition-colors"
                      >
                        Sign Up
                      </button>
                    </>
                  )}
                </div>
              )}
            </div>
          </div>
        </div>
      </nav>
      <section className="py-20 px-4 sm:px-6 lg:px-8">
        <div className="max-w-7xl mx-auto text-center">
          
          {/* Search Bar */}
          <div className="max-w-2xl mx-auto">
            <form onSubmit={handleSearch} className="flex flex-col sm:flex-row gap-4">
              <input
                type="text"
                placeholder="Search for a textbook..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="flex-1 px-4 py-3 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
              />
              <button 
                type="submit"
                className="bg-indigo-600 hover:bg-indigo-700 text-white px-8 py-3 rounded-lg font-medium transition-colors"
              >
                Search
              </button>
            </form>
          </div>
        </div>
      </section>
    </div>
  )
};

export default Header;