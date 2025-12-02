import { useState } from 'react';
import { reportListing } from '../services/reportsService';

const ReportModal = ({ listingId, listingTitle, onClose, reporterId }) => {
  const [reason, setReason] = useState('');
  const [customReason, setCustomReason] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  const predefinedReasons = [
    'Inappropriate content',
    'Spam or misleading',
    'Prohibited item',
    'Duplicate listing',
    'Other',
  ];

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!reason) {
      setError('Please select a reason');
      return;
    }

    if (reason === 'Other' && !customReason.trim()) {
      setError('Please provide a reason');
      return;
    }

    setSubmitting(true);
    setError('');

    try {
      const reportData = {
        reporterId: reporterId,
        targetType: 'LISTING',
        targetId: listingId,
        reason: reason === 'Other' ? customReason.trim() : reason,
      };

      await reportListing(reportData);
      setSuccess(true);
      
      // Close modal after 2 seconds
      setTimeout(() => {
        onClose();
      }, 2000);
    } catch (err) {
      console.error('Error reporting listing:', err);
      setError(err.message || 'Failed to submit report. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  if (success) {
    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-gray-800 rounded-lg shadow-xl w-full max-w-md p-6">
          <div className="text-center">
            <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-green-500 mb-4">
              <svg className="h-6 w-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
            </div>
            <h2 className="text-2xl font-bold text-white mb-2">Report Submitted</h2>
            <p className="text-gray-300">Thank you for your report. We'll review it shortly.</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-gray-800 rounded-lg shadow-xl w-full max-w-md p-6">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-2xl font-bold text-white">Report Listing</h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-white text-2xl font-bold"
          >
            ×
          </button>
        </div>

        {listingTitle && (
          <p className="text-gray-300 mb-4">Listing: {listingTitle}</p>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-2">
              Reason for Report
            </label>
            <div className="space-y-2">
              {predefinedReasons.map((predefinedReason) => (
                <label
                  key={predefinedReason}
                  className="flex items-center p-3 bg-gray-700 rounded-lg cursor-pointer hover:bg-gray-600 transition-colors"
                >
                  <input
                    type="radio"
                    name="reason"
                    value={predefinedReason}
                    checked={reason === predefinedReason}
                    onChange={(e) => {
                      setReason(e.target.value);
                      setError('');
                    }}
                    className="mr-3 w-4 h-4 text-indigo-600 focus:ring-indigo-500"
                  />
                  <span className="text-white">{predefinedReason}</span>
                </label>
              ))}
            </div>
          </div>

          {reason === 'Other' && (
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-2">
                Please specify the reason
              </label>
              <textarea
                value={customReason}
                onChange={(e) => {
                  setCustomReason(e.target.value);
                  setError('');
                }}
                className="w-full px-4 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
                rows={3}
                placeholder="Describe the issue..."
                required
              />
            </div>
          )}

          {error && (
            <div className="bg-red-900/50 border border-red-700 text-red-200 px-4 py-3 rounded-lg text-sm">
              {error}
            </div>
          )}

          <div className="flex gap-3">
            <button
              type="submit"
              disabled={submitting || !reason}
              className="flex-1 bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-lg font-medium disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {submitting ? 'Submitting...' : 'Submit Report'}
            </button>
            <button
              type="button"
              onClick={onClose}
              className="flex-1 bg-gray-600 hover:bg-gray-700 text-white px-4 py-2 rounded-lg font-medium"
            >
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ReportModal;

