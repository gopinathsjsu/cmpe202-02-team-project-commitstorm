import { useEffect } from 'react';

const SuccessModal = ({ message, onClose, autoCloseDelay = 2000 }) => {
  // Auto close after delay
  useEffect(() => {
    const timer = setTimeout(() => {
      onClose();
    }, autoCloseDelay);

    return () => clearTimeout(timer);
  }, [onClose, autoCloseDelay]);

  // Handle ESC key
  useEffect(() => {
    const handleEsc = (e) => {
      if (e.key === 'Escape') {
        onClose();
      }
    };

    document.addEventListener('keydown', handleEsc);
    return () => document.removeEventListener('keydown', handleEsc);
  }, [onClose]);

  return (
    <div 
      className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
      onClick={(e) => {
        if (e.target === e.currentTarget) {
          onClose();
        }
      }}
    >
      <div className="bg-gray-800 rounded-lg shadow-xl w-full max-w-md p-6">
        <div className="flex flex-col items-center justify-center text-center">
          {/* Success Icon - Green checkmark in a circle */}
          <div className="w-16 h-16 bg-green-500 rounded-full flex items-center justify-center mb-4">
            <svg
              className="w-10 h-10 text-white"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={3}
                d="M5 13l4 4L19 7"
              />
            </svg>
          </div>

          {/* Success Message */}
          <h2 className="text-2xl font-bold text-white mb-2">Success!</h2>
          <p className="text-gray-300 mb-6">{message || 'Operation completed successfully!'}</p>

          {/* Close Button */}
          <button
            onClick={onClose}
            className="bg-indigo-600 hover:bg-indigo-700 text-white px-6 py-2 rounded-lg font-medium transition-colors"
          >
            OK
          </button>
        </div>
      </div>
    </div>
  );
};

export default SuccessModal;

